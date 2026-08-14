DROP PROCEDURE IF EXISTS sp_desactivar_cliente;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_desactivar_cliente`(
    IN p_cliente_id BIGINT,
    IN p_empresa_id BIGINT
)
BEGIN

    DECLARE v_estado_actual TINYINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al desactivar el cliente' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    SELECT estado
    INTO v_estado_actual
    FROM clientes
    WHERE id = p_cliente_id
    AND empresa_id = p_empresa_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_NO_EXISTE' AS estado, 'El cliente no existe' AS mensaje, 0 AS id;

    ELSEIF v_estado_actual = 0 THEN
        ROLLBACK;
        SELECT 'ERROR_YA_DESACTIVADO' AS estado, 'El cliente ya está desactivado' AS mensaje, p_cliente_id AS id;

    ELSEIF v_estado_actual = -1 THEN
        ROLLBACK;
        SELECT 'ERROR_ELIMINADO' AS estado, 'El cliente fue eliminado definitivamente' AS mensaje, p_cliente_id AS id;

    ELSEIF EXISTS (
        SELECT 1 FROM clientes
        WHERE id = p_cliente_id AND numero_documento = '00000000'
    ) THEN
        -- El cliente genérico "CLIENTE VARIOS" nunca se desactiva
        ROLLBACK;
        SELECT 'ERROR_CLIENTE_GENERICO' AS estado, 'El cliente genérico no se puede desactivar' AS mensaje, p_cliente_id AS id;

    ELSE
        UPDATE clientes
        SET estado = 0, fecha_actualizacion = NOW()
        WHERE id = p_cliente_id
        AND empresa_id = p_empresa_id;

        COMMIT;
        SELECT 'OK' AS estado, 'Cliente desactivado correctamente' AS mensaje, p_cliente_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_desactivar_cliente
    Descripción:
        Desactiva (cambia estado a 0) un cliente. No lo elimina.
        El cliente genérico "CLIENTE VARIOS" (numero_documento = '00000000')
        nunca puede desactivarse porque lo usan las ventas sin cliente indicado.
    Estados posibles:
        OK, ERROR, ERROR_NO_EXISTE, ERROR_YA_DESACTIVADO, ERROR_ELIMINADO, ERROR_CLIENTE_GENERICO
    ===============================================================================
    */

END$$

DELIMITER ;
