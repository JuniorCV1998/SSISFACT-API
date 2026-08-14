DROP PROCEDURE IF EXISTS sp_listar_sucursales;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_sucursales`(
    IN p_empresa_id BIGINT,
    IN p_busqueda   VARCHAR(150),
    IN p_estado     INT,
    IN p_page       INT,
    IN p_size       INT
)
BEGIN

    DECLARE v_offset INT;
    SET v_offset = GREATEST(p_page - 1, 0) * p_size;

    SELECT
        s.id,
        s.empresa_id,
        s.nombre,
        s.direccion,
        s.telefono,
        s.estado,
        s.fecha_creacion,
        s.fecha_actualizacion,
        COUNT(*) OVER() AS total_registros
    FROM sucursales s
    WHERE s.empresa_id = p_empresa_id
    AND (
        p_estado = -1
        OR s.estado = p_estado
    )
    AND (
        p_busqueda IS NULL
        OR p_busqueda = ''
        OR s.nombre LIKE CONCAT('%', p_busqueda, '%')
    )
    ORDER BY s.nombre ASC
    LIMIT p_size OFFSET v_offset;

    /*
    ===============================================================================
    Nombre:
        sp_listar_sucursales
    Descripción:
        Lista las sucursales de una empresa, paginado, con filtro por nombre y
        por estado (1=Activas, 0=Inactivas, -1=Todas).
        Versión anterior (sin paginar, sin empresa_id/fechas/total_registros)
        quedó desalineada con SucursalRepositoryImpl, que ya llamaba con 5
        parámetros y esperaba esas columnas — causaba
        BadSqlGrammarException al listar.
    Tabla:
        - sucursales
    ===============================================================================
    */

END$$

DELIMITER ;
