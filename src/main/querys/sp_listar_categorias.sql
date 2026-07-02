DROP PROCEDURE IF EXISTS sp_listar_categorias;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_categorias`(
    IN p_empresa_id BIGINT,
    IN p_busqueda VARCHAR(150),
    IN p_estado TINYINT
)
BEGIN

    SELECT
        c.id,
        c.empresa_id,
        c.nombre,
        c.descripcion,
        c.estado,
        c.fecha_creacion,
        c.fecha_actualizacion
    FROM categorias c
    WHERE c.empresa_id = p_empresa_id
    AND (
        p_estado = -1
        OR c.estado = p_estado
    )
    AND (
        p_busqueda IS NULL
        OR p_busqueda = ''
        OR c.nombre LIKE CONCAT('%', p_busqueda, '%')
    )
    ORDER BY c.nombre ASC;

    /*
    ===============================================================================
    Nombre:
        sp_listar_categorias

    Descripción:
        Lista las categorías de una empresa, con filtro opcional por estado.

    Filtros:
        - Empresa
        - Nombre
        - Estado (1=Activas, 0=Inactivas, -1=Todas)

    Tabla:
        - categorias
    ===============================================================================
    */

END$$

DELIMITER ;
