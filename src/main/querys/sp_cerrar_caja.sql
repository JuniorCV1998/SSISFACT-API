DROP PROCEDURE IF EXISTS sp_cerrar_caja;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_cerrar_caja`(
    IN p_caja_id     BIGINT,
    IN p_usuario_id  BIGINT,
    IN p_monto_final DECIMAL(12,2)
)
BEGIN

    DECLARE v_estado_actual   VARCHAR(10);
    DECLARE v_usuario_dueno   BIGINT;
    DECLARE v_monto_inicial   DECIMAL(12,2);
    DECLARE v_monto_esperado  DECIMAL(12,2);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al cerrar la caja' AS mensaje, 0 AS id,
               0 AS monto_inicial, 0 AS monto_final, 0 AS monto_esperado, 0 AS diferencia;
    END;

    START TRANSACTION;

    SELECT estado, usuario_id, monto_inicial
    INTO v_estado_actual, v_usuario_dueno, v_monto_inicial
    FROM cajas
    WHERE id = p_caja_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_NO_EXISTE' AS estado, 'La caja no existe' AS mensaje, 0 AS id,
               0 AS monto_inicial, 0 AS monto_final, 0 AS monto_esperado, 0 AS diferencia;

    ELSEIF v_usuario_dueno <> p_usuario_id THEN
        ROLLBACK;
        SELECT 'ERROR_NO_AUTORIZADO' AS estado, 'Esta caja no te pertenece' AS mensaje, p_caja_id AS id,
               0 AS monto_inicial, 0 AS monto_final, 0 AS monto_esperado, 0 AS diferencia;

    ELSEIF v_estado_actual = 'CERRADA' THEN
        ROLLBACK;
        SELECT 'ERROR_YA_CERRADA' AS estado, 'La caja ya está cerrada' AS mensaje, p_caja_id AS id,
               0 AS monto_inicial, 0 AS monto_final, 0 AS monto_esperado, 0 AS diferencia;

    ELSE

        SELECT v_monto_inicial + COALESCE(SUM(p.monto), 0)
        INTO v_monto_esperado
        FROM pagos p
        INNER JOIN ventas v ON v.id = p.venta_id
        WHERE v.caja_id = p_caja_id
        AND v.estado <> 'ANULADA'
        AND p.metodo = 'EFECTIVO';

        UPDATE cajas
        SET estado = 'CERRADA',
            monto_final = p_monto_final,
            fecha_cierre = NOW()
        WHERE id = p_caja_id;

        COMMIT;

        SELECT 'OK' AS estado, 'Caja cerrada correctamente' AS mensaje, p_caja_id AS id,
               v_monto_inicial AS monto_inicial,
               p_monto_final AS monto_final,
               v_monto_esperado AS monto_esperado,
               (p_monto_final - v_monto_esperado) AS diferencia;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_cerrar_caja
    Descripción:
        Cierra una caja ABIERTA que pertenece al usuario. Calcula el monto en
        efectivo esperado (monto_inicial + pagos en EFECTIVO de las ventas no
        anuladas de esa caja) y la diferencia contra el monto contado (p_monto_final).
    Estados posibles:
        OK, ERROR, ERROR_NO_EXISTE, ERROR_NO_AUTORIZADO, ERROR_YA_CERRADA
    ===============================================================================
    */

END$$

DELIMITER ;
