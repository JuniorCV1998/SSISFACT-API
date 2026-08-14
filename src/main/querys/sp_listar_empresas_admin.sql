DROP PROCEDURE IF EXISTS sp_listar_empresas_admin;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_empresas_admin`(
    IN p_busqueda VARCHAR(150),
    IN p_estado   TINYINT,
    IN p_page     INT,
    IN p_size     INT
)
BEGIN

    DECLARE v_offset INT;
    SET v_offset = GREATEST(p_page - 1, 0) * p_size;

    SELECT
        e.id,
        e.nombre,
        e.ruc,
        e.email,
        e.telefono,
        e.estado,
        COALESCE(ce.plan, 'FREE') AS plan,
        COALESCE(ce.max_sucursal, 0) AS max_sucursal,
        COALESCE(ce.max_usuarios, 0) AS max_usuarios,
        ce.fecha_vencimiento,
        (SELECT COUNT(*) FROM sucursales s WHERE s.empresa_id = e.id AND s.estado = 1) AS total_sucursales,
        (SELECT COUNT(*) FROM usuarios u WHERE u.empresa_id = e.id AND u.estado = 1) AS total_usuarios,
        (SELECT COUNT(*) FROM ventas v WHERE v.empresa_id = e.id
            AND v.fecha >= DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS total_ventas_mes,
        e.fecha_creacion,
        COUNT(*) OVER() AS total_registros
    FROM empresas e
    LEFT JOIN configuracion_empresa ce ON ce.empresa_id = e.id
    WHERE e.ruc <> '00000000000'
    AND (p_estado = -1 OR e.estado = p_estado)
    AND (
        p_busqueda IS NULL
        OR p_busqueda = ''
        OR e.nombre LIKE CONCAT('%', p_busqueda, '%')
        OR e.ruc LIKE CONCAT('%', p_busqueda, '%')
    )
    ORDER BY e.nombre ASC
    LIMIT p_size OFFSET v_offset;

    /*
    ===============================================================================
    Nombre:
        sp_listar_empresas_admin
    Descripción:
        Panel superadmin: lista todas las empresas de la plataforma (excluye la
        empresa fantasma del propio superadmin, ruc='00000000000'), con su plan,
        límites, vencimiento y contadores de uso (sucursales/usuarios activos,
        ventas del mes en curso).
    Filtros:
        - Búsqueda por nombre o RUC
        - Estado (1=activas, 0=eliminadas, 2=pendientes, -1=todas)
    ===============================================================================
    */

END$$

DELIMITER ;
