DROP PROCEDURE IF EXISTS sp_listar_usuarios_empresa_admin;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_usuarios_empresa_admin`(
    IN p_empresa_id BIGINT
)
BEGIN

    SELECT
        u.id,
        u.nombre,
        u.email,
        u.documento,
        u.sucursal_id,
        u.estado,
        GROUP_CONCAT(r.nombre ORDER BY r.nombre SEPARATOR ',') AS roles,
        u.fecha_creacion
    FROM usuarios u
    LEFT JOIN usuario_roles ur ON ur.usuario_id = u.id
    LEFT JOIN roles r ON r.id = ur.rol_id
    WHERE u.empresa_id = p_empresa_id
    GROUP BY u.id
    ORDER BY u.nombre ASC;

    /*
    ===============================================================================
    Nombre:
        sp_listar_usuarios_empresa_admin
    Descripción:
        Panel superadmin: usuarios de una empresa con sus roles concatenados
        (separados por coma), para poder reasignar el rol de alguno.
    ===============================================================================
    */

END$$

DELIMITER ;
