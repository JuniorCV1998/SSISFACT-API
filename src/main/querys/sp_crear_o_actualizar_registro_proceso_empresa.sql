DROP PROCEDURE IF EXISTS sp_crear_o_actualizar_registro_proceso_empresa;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_o_actualizar_registro_proceso_empresa`(
    IN p_idRegisterProcess INT,
    IN p_ruc VARCHAR(11),
    IN p_razon_social VARCHAR(120),
    IN p_direccion VARCHAR(120),
    IN p_telefono VARCHAR(50),
    IN p_email_empresa VARCHAR(100),
    IN p_documento VARCHAR(12),
    IN p_nombre_admin VARCHAR(120),
    IN p_email_admin VARCHAR(100),
    IN p_contrasena VARCHAR(255),
    IN p_confirmar TINYINT
)
BEGIN

    DECLARE v_id INT;
    DECLARE v_id_empresa INT;
    DECLARE v_id_usuario INT;
    DECLARE v_id_existente INT;

    DECLARE v_ruc VARCHAR(11);
    DECLARE v_razon_social VARCHAR(120);
    DECLARE v_direccion VARCHAR(120);
    DECLARE v_telefono VARCHAR(50);
    DECLARE v_email_empresa VARCHAR(100);
    DECLARE v_email_admin VARCHAR(100);
    DECLARE v_contrasena VARCHAR(255);
    DECLARE v_documento VARCHAR(12);
    DECLARE v_nombre_admin VARCHAR(120);

    -- Valores dinámicos desde configuracion_sistema
    DECLARE v_cfg_moneda_id    BIGINT       DEFAULT 1;
    DECLARE v_cfg_impuesto     DECIMAL(5,2) DEFAULT 18.00;
    DECLARE v_cfg_max_sucursal INT          DEFAULT 5;
    DECLARE v_cfg_max_usuarios INT          DEFAULT 5;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error en el proceso' AS mensaje, 0 AS id;
    END;

    -- =========================================
    -- MODO CONFIRMAR
    -- =========================================
    IF p_confirmar = 1 THEN

        START TRANSACTION;

        IF NOT EXISTS (
            SELECT 1 FROM register_process WHERE idRegisterProcess = p_idRegisterProcess
        ) THEN
            SELECT 'ERROR' AS estado, 'Proceso no encontrado' AS mensaje, 0 AS id;

        ELSE

            SELECT ruc, razonSocial, telefono, direccion, correo_empresa,
                   correo_usuario, contrasena, documento, nombre
            INTO   v_ruc, v_razon_social, v_telefono, v_direccion, v_email_empresa,
                   v_email_admin, v_contrasena, v_documento, v_nombre_admin
            FROM register_process
            WHERE idRegisterProcess = p_idRegisterProcess;

            IF EXISTS (SELECT 1 FROM empresas WHERE ruc = v_ruc) THEN
                ROLLBACK;
                SELECT 'ERROR_RUC_EXISTE' AS estado, 'El RUC ya fue registrado por otro usuario' AS mensaje, 0 AS id;

            ELSEIF EXISTS (SELECT 1 FROM empresas WHERE email = v_email_empresa) THEN
                ROLLBACK;
                SELECT 'ERROR_CORREO_EMPRESA' AS estado, 'El correo de empresa ya fue registrado' AS mensaje, 0 AS id;

            ELSEIF EXISTS (SELECT 1 FROM usuarios WHERE documento = v_documento) THEN
                SELECT 'ERROR_DOCUMENTO_EXISTE' AS estado, 'El número de documento ya está registrado' AS mensaje, 0 AS id;

            ELSEIF EXISTS (SELECT 1 FROM usuarios WHERE email = v_email_admin) THEN
                ROLLBACK;
                SELECT 'ERROR_CORREO_ADMIN' AS estado, 'El correo del administrador ya fue registrado' AS mensaje, 0 AS id;

            ELSEIF EXISTS (SELECT 1 FROM usuarios WHERE documento = v_documento) THEN
                ROLLBACK;
                SELECT 'ERROR_DOCUMENTO' AS estado, 'El documento ya fue registrado' AS mensaje, 0 AS id;

            ELSE

                -- INSERT EMPRESA
                INSERT INTO empresas (nombre, ruc, direccion, telefono, email, estado, fecha_creacion)
                VALUES (v_razon_social, v_ruc, v_direccion, v_telefono, v_email_empresa, 2, NOW());

                SET v_id_empresa = LAST_INSERT_ID();

                -- ============================================================
                -- LEER CONFIGURACIÓN DEL SISTEMA (dinámica)
                -- ============================================================
                SELECT moneda_id, impuesto_default, max_sucursal_default, max_usuarios_default
                INTO   v_cfg_moneda_id, v_cfg_impuesto, v_cfg_max_sucursal, v_cfg_max_usuarios
                FROM configuracion_sistema
                WHERE tipo_configuracion = 'PERU'
                AND estado = 1
                LIMIT 1;

                -- ============================================================
                -- CREAR CONFIGURACIÓN DE EMPRESA (con valores del sistema)
                -- ============================================================
                INSERT INTO configuracion_empresa (
                    empresa_id, moneda_id, impuesto, max_sucursal, max_usuarios, logo_url, fecha_actualizacion
                ) VALUES (
                    v_id_empresa,
                    v_cfg_moneda_id,
                    v_cfg_impuesto,
                    v_cfg_max_sucursal,
                    v_cfg_max_usuarios,
                    NULL,
                    NOW()
                );

                -- ============================================================
                -- CREAR SUCURSAL PRINCIPAL
                -- ============================================================
                INSERT INTO sucursales (empresa_id, nombre, direccion, telefono, estado, fecha_creacion)
                VALUES (v_id_empresa, CONCAT('Sucursal Principal - ', v_razon_social), v_direccion, v_telefono, 1, NOW());

                -- ============================================================
                -- CLIENTE GENÉRICO (para ventas sin cliente identificado)
                -- ============================================================
                INSERT INTO clientes (empresa_id, tipo_documento, numero_documento, nombre, estado, fecha_creacion)
                VALUES (v_id_empresa, 'DNI', '00000000', 'CLIENTE VARIOS', 1, NOW());

                -- ============================================================
                -- SERIES PRINCIPALES (boleta simple y guía, sin valor tributario)
                -- ============================================================
                INSERT INTO series (empresa_id, tipo, serie, correlativo_actual, es_principal, estado)
                VALUES (v_id_empresa, 'BOLETA', 'B001', 0, 1, 1),
                       (v_id_empresa, 'GUIA', 'T001', 0, 1, 1);

                -- INSERT USUARIO
                INSERT INTO usuarios (empresa_id, nombre, email, documento, contrasena, estado, fecha_creacion)
                VALUES (v_id_empresa, v_nombre_admin, v_email_admin, v_documento, v_contrasena, 1, NOW());

                SET v_id_usuario = LAST_INSERT_ID();

                -- ASIGNAR ROL ADMIN
                INSERT INTO usuario_roles (usuario_id, rol_id, fecha_creacion)
                VALUES (v_id_usuario, 1, NOW());

                -- ACTUALIZAR PROCESO
                UPDATE register_process
                SET estado = 'I', date_update = NOW()
                WHERE idRegisterProcess = p_idRegisterProcess;

                COMMIT;

                SELECT 'CONFIRMADO' AS estado,
                       CONCAT('Empresa ', v_razon_social, ' creada correctamente') AS mensaje,
                       v_id_empresa AS id;

            END IF;

        END IF;

    -- =========================================
    -- MODO NORMAL (registrar / actualizar proceso)
    -- =========================================
    ELSE

        IF EXISTS (SELECT 1 FROM empresas WHERE ruc = p_ruc) THEN
            SELECT 'ERROR_RUC_EXISTE' AS estado, 'El RUC ya está registrado' AS mensaje, 0 AS id;

        ELSEIF EXISTS (SELECT 1 FROM empresas WHERE email = p_email_empresa) THEN
            SELECT 'ERROR_CORREO_EMPRESA' AS estado, 'El correo de empresa ya está registrado' AS mensaje, 0 AS id;

        ELSEIF EXISTS (SELECT 1 FROM usuarios WHERE documento = p_documento) THEN
            SELECT 'ERROR_DOCUMENTO_EXISTE' AS estado, 'El número de documento ya está registrado' AS mensaje, 0 AS id;

        ELSEIF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email_admin) THEN
            SELECT 'ERROR_CORREO_ADMIN' AS estado, 'El correo del administrador ya está registrado' AS mensaje, 0 AS id;

        ELSE

            SELECT idRegisterProcess INTO v_id_existente
            FROM register_process
            WHERE ruc = p_ruc AND estado = 'A'
            ORDER BY date_create DESC
            LIMIT 1;

            IF p_idRegisterProcess IS NULL THEN

                IF v_id_existente IS NOT NULL THEN

                    UPDATE register_process
                    SET razonSocial     = p_razon_social,
                        telefono        = p_telefono,
                        direccion       = p_direccion,
                        correo_empresa  = p_email_empresa,
                        correo_usuario  = p_email_admin,
                        contrasena      = p_contrasena,
                        documento       = p_documento,
                        nombre          = p_nombre_admin,
                        date_update     = NOW()
                    WHERE idRegisterProcess = v_id_existente;

                    SET v_id = v_id_existente;
                    SELECT 'OK' AS estado, 'Registro actualizado correctamente' AS mensaje, v_id AS id;

                ELSE

                    INSERT INTO register_process (
                        ruc, razonSocial, telefono, direccion, correo_empresa,
                        correo_usuario, contrasena, documento, nombre, estado, date_create
                    ) VALUES (
                        p_ruc, p_razon_social, p_telefono, p_direccion, p_email_empresa,
                        p_email_admin, p_contrasena, p_documento, p_nombre_admin, 'A', NOW()
                    );

                    SET v_id = LAST_INSERT_ID();
                    SELECT 'OK' AS estado, 'Registro guardado correctamente' AS mensaje, v_id AS id;

                END IF;

            ELSE

                IF NOT EXISTS (SELECT 1 FROM register_process WHERE idRegisterProcess = p_idRegisterProcess) THEN
                    SET v_id = p_idRegisterProcess;
                    SELECT 'ERROR_ID_NO_EXISTE' AS estado, 'El registro no existe' AS mensaje, v_id AS id;

                ELSE

                    UPDATE register_process
                    SET telefono        = p_telefono,
                        direccion       = p_direccion,
                        correo_empresa  = p_email_empresa,
                        correo_usuario  = p_email_admin,
                        contrasena      = p_contrasena,
                        nombre          = p_nombre_admin,
                        date_update     = NOW()
                    WHERE idRegisterProcess = p_idRegisterProcess;

                    SET v_id = p_idRegisterProcess;
                    SELECT 'OK' AS estado, 'Registro actualizado correctamente' AS mensaje, v_id AS id;

                END IF;

            END IF;

        END IF;

    END IF;

END$$

DELIMITER ;
