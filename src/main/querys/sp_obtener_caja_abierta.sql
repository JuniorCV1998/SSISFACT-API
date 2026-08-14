DROP PROCEDURE IF EXISTS sp_obtener_caja_abierta;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_caja_abierta`(
    IN p_usuario_id BIGINT
)
BEGIN

    SELECT
        c.id,
        c.sucursal_id,
        s.nombre AS sucursal_nombre,
        c.usuario_id,
        c.monto_inicial,
        c.monto_final,
        c.fecha_apertura,
        c.fecha_cierre,
        c.estado
    FROM cajas c
    INNER JOIN sucursales s ON s.id = c.sucursal_id
    WHERE c.usuario_id = p_usuario_id
    AND c.estado = 'ABIERTA'
    LIMIT 1;

    /*
    ===============================================================================
    Nombre:
        sp_obtener_caja_abierta
    Descripción:
        Devuelve la caja ABIERTA del usuario (si existe). Sin filas si no tiene
        ninguna caja abierta — el usuario debe abrir una antes de vender.
    ===============================================================================
    */

END$$

DELIMITER ;
