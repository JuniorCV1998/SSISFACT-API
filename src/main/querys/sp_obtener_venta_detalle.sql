DROP PROCEDURE IF EXISTS sp_obtener_venta_detalle;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_venta_detalle`(
    IN p_venta_id   BIGINT,
    IN p_empresa_id BIGINT
)
BEGIN

    -- Cabecera de la venta con cliente, usuario, sucursal y comprobante.
    -- Los ítems (detalle_venta) y los pagos se consultan aparte, con SELECTs
    -- simples desde VentaRepositoryImpl (son joins de una sola tabla, no
    -- ameritan una SP propia).

    SELECT
        v.id,
        v.empresa_id,
        e.nombre AS empresa_nombre,
        e.ruc AS empresa_ruc,
        e.direccion AS empresa_direccion,
        v.sucursal_id,
        s.nombre AS sucursal_nombre,
        v.cliente_id,
        c.nombre AS cliente_nombre,
        c.tipo_documento AS cliente_tipo_documento,
        c.numero_documento AS cliente_documento,
        v.usuario_id,
        u.nombre AS usuario_nombre,
        v.caja_id,
        v.subtotal,
        v.descuento,
        v.impuestos,
        v.total,
        v.estado,
        v.fecha,
        cp.id AS comprobante_id,
        cp.tipo AS comprobante_tipo,
        cp.serie AS comprobante_serie,
        cp.numero AS comprobante_numero
    FROM ventas v
    INNER JOIN empresas e ON e.id = v.empresa_id
    INNER JOIN sucursales s ON s.id = v.sucursal_id
    INNER JOIN clientes c ON c.id = v.cliente_id
    INNER JOIN usuarios u ON u.id = v.usuario_id
    LEFT JOIN comprobantes cp ON cp.venta_id = v.id
    WHERE v.id = p_venta_id
    AND v.empresa_id = p_empresa_id;

    /*
    ===============================================================================
    Nombre:
        sp_obtener_venta_detalle
    Descripción:
        Cabecera completa de una venta (una fila) para la pantalla de detalle.
    ===============================================================================
    */

END$$

DELIMITER ;
