DROP PROCEDURE IF EXISTS sp_obtener_empresa_admin;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_empresa_admin`(
    IN p_empresa_id BIGINT
)
BEGIN

    SELECT
        e.id,
        e.nombre,
        e.ruc,
        e.email,
        e.telefono,
        e.direccion,
        e.estado,
        COALESCE(ce.plan, 'FREE') AS plan,
        COALESCE(ce.max_sucursal, 0) AS max_sucursal,
        COALESCE(ce.max_usuarios, 0) AS max_usuarios,
        ce.fecha_vencimiento,
        (SELECT COUNT(*) FROM sucursales s WHERE s.empresa_id = e.id AND s.estado = 1) AS total_sucursales,
        (SELECT COUNT(*) FROM usuarios u WHERE u.empresa_id = e.id AND u.estado = 1) AS total_usuarios,
        (SELECT COUNT(*) FROM ventas v WHERE v.empresa_id = e.id
            AND v.fecha >= DATE_FORMAT(CURDATE(), '%Y-%m-01')) AS total_ventas_mes,
        e.fecha_creacion
    FROM empresas e
    LEFT JOIN configuracion_empresa ce ON ce.empresa_id = e.id
    WHERE e.id = p_empresa_id;

    /*
    ===============================================================================
    Nombre:
        sp_obtener_empresa_admin
    Descripción:
        Panel superadmin: detalle de una empresa (una fila), con plan, límites
        y contadores de uso. El estado de credenciales SUNAT se agrega en Java
        (AdminEmpresaServiceImpl) reutilizando
        SunatService.tieneCredencialesConfiguradas, que consulta la tabla
        sunat_credenciales — no las columnas username_sunat/password_sunat de
        empresas, que no son las que usa el módulo SUNAT real.
    ===============================================================================
    */

END$$

DELIMITER ;
