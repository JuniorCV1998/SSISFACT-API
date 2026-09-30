DROP PROCEDURE IF EXISTS sp_actualizar_empresa;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_empresa`(
    IN p_empresa_id BIGINT,
    IN p_nombre VARCHAR(150),
    IN p_ruc VARCHAR(20),
    IN p_direccion VARCHAR(255),
    IN p_telefono VARCHAR(50),
    IN p_email VARCHAR(100),
    IN p_igv_habilitado TINYINT,
    IN p_igv_porcentaje DECIMAL(5,2),
    IN p_ticket_mensaje_pie VARCHAR(255)
)
BEGIN

    DECLARE v_estado_actual TINYINT;

    -- Handler para errores SQL
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT
            'ERROR' AS estado,
            'Error al actualizar la empresa' AS mensaje,
            0 AS id;
    END;

    START TRANSACTION;

    -- ============================================================
    -- VALIDAR QUE EXISTE Y ESTÁ ACTIVA
    -- ============================================================

    SELECT estado
    INTO v_estado_actual
    FROM empresas
    WHERE id = p_empresa_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT
            'ERROR_NO_EXISTE' AS estado,
            'La empresa no existe' AS mensaje,
            0 AS id;

    -- 0=eliminada (bloquea). El resto de valores (1, 2) son estados operables.
    ELSEIF v_estado_actual = 0 THEN
        ROLLBACK;
        SELECT
            'ERROR_ELIMINADA' AS estado,
            'La empresa fue eliminada' AS mensaje,
            0 AS id;

    -- Validar RUC duplicado (diferente a la propia empresa)
    ELSEIF EXISTS (
        SELECT 1 FROM empresas WHERE ruc = p_ruc AND id <> p_empresa_id
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_RUC_EXISTE' AS estado,
            'El RUC ya está registrado por otra empresa' AS mensaje,
            0 AS id;

    -- Validar email duplicado (diferente a la propia empresa), solo si viene
    ELSEIF p_email IS NOT NULL AND TRIM(p_email) <> '' AND EXISTS (
        SELECT 1 FROM empresas WHERE email = p_email AND id <> p_empresa_id
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_EMAIL_EXISTE' AS estado,
            'El email ya está registrado por otra empresa' AS mensaje,
            0 AS id;

    ELSE

        UPDATE empresas
        SET
            nombre = p_nombre,
            ruc = p_ruc,
            direccion = p_direccion,
            telefono = p_telefono,
            email = IF(p_email = '', NULL, p_email),
            fecha_actualizacion = NOW()
        WHERE id = p_empresa_id;

        UPDATE configuracion_empresa
        SET
            igv_habilitado = p_igv_habilitado,
            impuesto = p_igv_porcentaje,
            ticket_mensaje_pie = p_ticket_mensaje_pie,
            fecha_actualizacion = NOW()
        WHERE empresa_id = p_empresa_id;

        COMMIT;

        SELECT
            'OK' AS estado,
            'Empresa actualizada correctamente' AS mensaje,
            p_empresa_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_actualizar_empresa
    Autor:
        Jose Cerron
    Descripción:
        Actualiza los datos de perfil de la empresa (usados en Configuración) y
        su fila en configuracion_empresa (IGV y mensaje de pie del ticket).
    Parámetros:
        p_empresa_id: ID de la empresa (resuelta del JWT, nunca viaja del cliente)
        p_nombre, p_ruc, p_direccion, p_telefono, p_email: datos de perfil
        p_igv_habilitado: 0/1
        p_igv_porcentaje: porcentaje de IGV a aplicar cuando esté habilitado
        p_ticket_mensaje_pie: texto libre al pie del ticket impreso
    Validaciones:
        - Empresa existe y está activa
        - RUC no está en uso por otra empresa
        - Email (si viene) no está en uso por otra empresa
    Estados posibles:
        OK
        ERROR
        ERROR_NO_EXISTE
        ERROR_ELIMINADA
        ERROR_RUC_EXISTE
        ERROR_EMAIL_EXISTE
    Tablas involucradas:
        - empresas
        - configuracion_empresa
    ===============================================================================
    */

END$$

DELIMITER ;
