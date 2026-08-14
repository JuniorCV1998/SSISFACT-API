DROP PROCEDURE IF EXISTS sp_listar_ventas;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_ventas`(
    IN p_empresa_id   BIGINT,
    IN p_sucursal_id  BIGINT,
    IN p_cliente_id   BIGINT,
    IN p_caja_id      BIGINT,
    IN p_estado       VARCHAR(10),
    IN p_fecha_desde  DATE,
    IN p_fecha_hasta  DATE,
    IN p_page         INT,
    IN p_size         INT
)
BEGIN

    DECLARE v_offset INT;
    SET v_offset = GREATEST(p_page - 1, 0) * p_size;

    SELECT
        v.id,
        v.sucursal_id,
        s.nombre AS sucursal_nombre,
        v.cliente_id,
        c.nombre AS cliente_nombre,
        c.numero_documento AS cliente_documento,
        v.usuario_id,
        u.nombre AS usuario_nombre,
        v.subtotal,
        v.descuento,
        v.impuestos,
        v.total,
        v.estado,
        v.fecha,
        cp.tipo AS comprobante_tipo,
        cp.serie AS comprobante_serie,
        cp.numero AS comprobante_numero,
        COUNT(*) OVER() AS total_registros
    FROM ventas v
    INNER JOIN sucursales s ON s.id = v.sucursal_id
    INNER JOIN clientes c ON c.id = v.cliente_id
    INNER JOIN usuarios u ON u.id = v.usuario_id
    LEFT JOIN comprobantes cp ON cp.venta_id = v.id
    WHERE v.empresa_id = p_empresa_id
    AND (p_sucursal_id IS NULL OR v.sucursal_id = p_sucursal_id)
    AND (p_cliente_id IS NULL OR v.cliente_id = p_cliente_id)
    AND (p_caja_id IS NULL OR v.caja_id = p_caja_id)
    AND (p_estado IS NULL OR p_estado = '' OR v.estado = p_estado)
    AND (p_fecha_desde IS NULL OR v.fecha >= p_fecha_desde)
    AND (p_fecha_hasta IS NULL OR v.fecha < DATE_ADD(p_fecha_hasta, INTERVAL 1 DAY))
    ORDER BY v.fecha DESC
    LIMIT p_size OFFSET v_offset;

    /*
    ===============================================================================
    Nombre:
        sp_listar_ventas
    Descripción:
        Lista las ventas de una empresa con filtros opcionales de sucursal,
        cliente, caja, estado y rango de fechas, paginado. Incluye el comprobante
        (tipo/serie/número) asociado si existe.
    ===============================================================================
    */

END$$

DELIMITER ;
