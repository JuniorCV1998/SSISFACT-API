DROP PROCEDURE IF EXISTS sp_abrir_caja;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_abrir_caja`(
    IN p_sucursal_id   BIGINT,
    IN p_usuario_id    BIGINT,
    IN p_monto_inicial DECIMAL(12,2)
)
BEGIN

    DECLARE v_caja_id BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al abrir la caja' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    IF NOT EXISTS (
        SELECT 1 FROM sucursales WHERE id = p_sucursal_id AND estado = 1
    ) THEN
        ROLLBACK;
        SELECT 'ERROR_SUCURSAL' AS estado, 'Sucursal no encontrada' AS mensaje, 0 AS id;

    ELSEIF p_monto_inicial IS NULL OR p_monto_inicial < 0 THEN
        ROLLBACK;
        SELECT 'ERROR_MONTO' AS estado, 'El monto inicial no puede ser negativo' AS mensaje, 0 AS id;

    ELSEIF EXISTS (
        SELECT 1 FROM cajas WHERE usuario_id = p_usuario_id AND estado = 'ABIERTA'
    ) THEN
        ROLLBACK;
        SELECT 'ERROR_CAJA_ABIERTA' AS estado, 'Ya tienes una caja abierta' AS mensaje, 0 AS id;

    ELSE
        INSERT INTO cajas (sucursal_id, usuario_id, monto_inicial, estado, fecha_apertura)
        VALUES (p_sucursal_id, p_usuario_id, p_monto_inicial, 'ABIERTA', NOW());

        SET v_caja_id = LAST_INSERT_ID();
        COMMIT;
        SELECT 'OK' AS estado, 'Caja abierta correctamente' AS mensaje, v_caja_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_abrir_caja
    Descripción:
        Abre una caja para un usuario en una sucursal. Un usuario no puede tener
        más de una caja ABIERTA a la vez.
    Estados posibles:
        OK, ERROR, ERROR_SUCURSAL, ERROR_MONTO, ERROR_CAJA_ABIERTA
    ===============================================================================
    */

END$$

DELIMITER ;
