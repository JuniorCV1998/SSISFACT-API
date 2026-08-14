DROP PROCEDURE IF EXISTS sp_crear_o_actualizar_cliente;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_o_actualizar_cliente`(
    IN p_id_cliente       BIGINT,
    IN p_empresa_id       BIGINT,
    IN p_tipo_documento   VARCHAR(20),
    IN p_numero_documento VARCHAR(20),
    IN p_nombre           VARCHAR(150),
    IN p_telefono         VARCHAR(20),
    IN p_email            VARCHAR(100),
    IN p_direccion        VARCHAR(255)
)
BEGIN

    DECLARE v_cliente_id BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error en el proceso' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    -- ============================================================
    -- VALIDAR EMPRESA
    -- ============================================================

    IF p_empresa_id IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_EMPRESA' AS estado, 'Debe indicar una empresa válida' AS mensaje, 0 AS id;

    ELSEIF p_tipo_documento NOT IN ('DNI','RUC','CE','PASAPORTE') THEN
        ROLLBACK;
        SELECT 'ERROR_TIPO_DOCUMENTO' AS estado, 'Tipo de documento inválido' AS mensaje, 0 AS id;

    ELSE

        -- ============================================================
        -- CREAR
        -- ============================================================

        IF p_id_cliente IS NULL THEN

            -- Excluye estado=-1: los documentos de clientes eliminados quedan libres
            IF EXISTS (
                SELECT 1 FROM clientes
                WHERE empresa_id = p_empresa_id
                AND tipo_documento = p_tipo_documento
                AND numero_documento = TRIM(p_numero_documento)
                AND estado <> -1
            ) THEN
                ROLLBACK;
                SELECT 'ERROR_CLIENTE_EXISTE' AS estado, 'Ya existe un cliente con ese documento' AS mensaje, 0 AS id;

            ELSE
                INSERT INTO clientes (
                    empresa_id, tipo_documento, numero_documento, nombre,
                    telefono, email, direccion, estado, fecha_creacion
                ) VALUES (
                    p_empresa_id, p_tipo_documento, TRIM(p_numero_documento), TRIM(p_nombre),
                    p_telefono, p_email, p_direccion, 1, NOW()
                );

                SET v_cliente_id = LAST_INSERT_ID();
                COMMIT;
                SELECT 'OK' AS estado, CONCAT('Cliente ', p_nombre, ' creado correctamente') AS mensaje, v_cliente_id AS id;
            END IF;

        ELSE

            -- ============================================================
            -- ACTUALIZAR
            -- ============================================================

            IF NOT EXISTS (
                SELECT 1 FROM clientes
                WHERE id = p_id_cliente AND empresa_id = p_empresa_id
            ) THEN
                ROLLBACK;
                SELECT 'ERROR_CLIENTE' AS estado, 'El cliente no existe' AS mensaje, 0 AS id;

            ELSE
                IF EXISTS (
                    SELECT 1 FROM clientes
                    WHERE empresa_id = p_empresa_id
                    AND tipo_documento = p_tipo_documento
                    AND numero_documento = TRIM(p_numero_documento)
                    AND id <> p_id_cliente
                    AND estado <> -1
                ) THEN
                    ROLLBACK;
                    SELECT 'ERROR_CLIENTE_EXISTE' AS estado, 'Ya existe un cliente con ese documento' AS mensaje, 0 AS id;

                ELSE
                    UPDATE clientes
                    SET tipo_documento = p_tipo_documento,
                        numero_documento = TRIM(p_numero_documento),
                        nombre = TRIM(p_nombre),
                        telefono = p_telefono,
                        email = p_email,
                        direccion = p_direccion,
                        fecha_actualizacion = NOW()
                    WHERE id = p_id_cliente;

                    COMMIT;
                    SELECT 'OK' AS estado, CONCAT('Cliente ', p_nombre, ' actualizado correctamente') AS mensaje, p_id_cliente AS id;
                END IF;

            END IF;

        END IF;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_crear_o_actualizar_cliente
    Descripción:
        Registra o actualiza un cliente. La validación de documento duplicado
        excluye registros con estado=-1 (eliminados definitivamente).
    Estados posibles:
        OK, ERROR, ERROR_EMPRESA, ERROR_TIPO_DOCUMENTO, ERROR_CLIENTE, ERROR_CLIENTE_EXISTE
    ===============================================================================
    */

END$$

DELIMITER ;
