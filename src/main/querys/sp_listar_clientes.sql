DROP PROCEDURE IF EXISTS sp_listar_clientes;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_clientes`(
    IN p_empresa_id BIGINT,
    IN p_busqueda   VARCHAR(150),
    IN p_estado     TINYINT,
    IN p_page       INT,
    IN p_size       INT
)
BEGIN

    DECLARE v_offset INT;
    SET v_offset = GREATEST(p_page - 1, 0) * p_size;

    SELECT
        c.id,
        c.empresa_id,
        c.tipo_documento,
        c.numero_documento,
        c.nombre,
        c.telefono,
        c.email,
        c.direccion,
        c.estado,
        c.fecha_creacion,
        c.fecha_actualizacion,
        COUNT(*) OVER() AS total_registros
    FROM clientes c
    WHERE c.empresa_id = p_empresa_id
    AND (
        p_estado = -1
        OR c.estado = p_estado
    )
    AND (
        p_busqueda IS NULL
        OR p_busqueda = ''
        OR c.nombre LIKE CONCAT('%', p_busqueda, '%')
        OR c.numero_documento LIKE CONCAT('%', p_busqueda, '%')
    )
    ORDER BY c.nombre ASC
    LIMIT p_size OFFSET v_offset;

    /*
    ===============================================================================
    Nombre:
        sp_listar_clientes
    Descripción:
        Lista los clientes de una empresa, paginado, con filtro por nombre o
        número de documento y por estado (1=Activos, 0=Inactivos, -1=Todos).
    Tabla:
        - clientes
    ===============================================================================
    */

END$$

DELIMITER ;
