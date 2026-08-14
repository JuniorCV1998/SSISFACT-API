DROP PROCEDURE IF EXISTS sp_resumen_caja;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_resumen_caja`(
    IN p_caja_id    BIGINT,
    IN p_empresa_id BIGINT
)
BEGIN

    SELECT
        c.id,
        c.sucursal_id,
        s.nombre AS sucursal_nombre,
        c.usuario_id,
        u.nombre AS usuario_nombre,
        c.monto_inicial,
        c.monto_final,
        c.fecha_apertura,
        c.fecha_cierre,
        c.estado,
        COUNT(DISTINCT v.id) AS cantidad_ventas,
        COALESCE(SUM(v.total), 0) AS total_vendido
    FROM cajas c
    INNER JOIN sucursales s ON s.id = c.sucursal_id
    INNER JOIN usuarios u ON u.id = c.usuario_id
    LEFT JOIN ventas v ON v.caja_id = c.id AND v.estado <> 'ANULADA'
    WHERE c.id = p_caja_id
    AND s.empresa_id = p_empresa_id
    GROUP BY c.id, c.sucursal_id, s.nombre, c.usuario_id, u.nombre, c.monto_inicial,
             c.monto_final, c.fecha_apertura, c.fecha_cierre, c.estado;

    /*
    ===============================================================================
    Nombre:
        sp_resumen_caja
    Descripción:
        Cabecera de una caja (abierta o cerrada) de una empresa, con cantidad
        de ventas y total vendido (sin contar las anuladas). El desglose por
        método de pago y por producto vendido se consulta aparte desde
        CajaRepositoryImpl (joins de una sola tabla, mismo criterio ya usado
        para ítems/pagos del detalle de venta). Devuelve 0 filas si la caja
        no existe o no pertenece a la empresa.
    ===============================================================================
    */

END$$

DELIMITER ;
