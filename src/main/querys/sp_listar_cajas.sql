DROP PROCEDURE IF EXISTS sp_listar_cajas;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_cajas`(
    IN p_empresa_id  BIGINT,
    IN p_sucursal_id BIGINT,
    IN p_estado      VARCHAR(10),
    IN p_page        INT,
    IN p_size        INT
)
BEGIN

    DECLARE v_offset INT;
    SET v_offset = GREATEST(p_page - 1, 0) * p_size;

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
        COUNT(*) OVER() AS total_registros
    FROM cajas c
    INNER JOIN sucursales s ON s.id = c.sucursal_id
    INNER JOIN usuarios u ON u.id = c.usuario_id
    WHERE s.empresa_id = p_empresa_id
    AND (p_sucursal_id IS NULL OR c.sucursal_id = p_sucursal_id)
    AND (p_estado IS NULL OR p_estado = '' OR c.estado = p_estado)
    ORDER BY c.fecha_apertura DESC
    LIMIT p_size OFFSET v_offset;

    /*
    ===============================================================================
    Nombre:
        sp_listar_cajas
    Descripción:
        Historial de cajas de una empresa, con filtro opcional por sucursal y
        por estado (ABIERTA/CERRADA), paginado.
    ===============================================================================
    */

END$$

DELIMITER ;
