DROP PROCEDURE IF EXISTS sp_listar_sucursales;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_sucursales`(
    IN p_empresa_id BIGINT
)
BEGIN

    SELECT
        s.id,
        s.nombre,
        s.direccion,
        s.telefono,
        s.estado,
        COALESCE(ce.max_sucursal, 10) AS max_sucursal
    FROM sucursales s
    LEFT JOIN configuracion_empresa ce ON ce.empresa_id = s.empresa_id
    WHERE s.empresa_id = p_empresa_id
    AND s.estado IN (0, 1)
    ORDER BY s.nombre ASC;

    /*
    ===============================================================================
    Nombre:
        sp_listar_sucursales
    Autor:
        Jose Cerron
    Descripción:
        Lista las sucursales activas (1) e inactivas (0) de una empresa.
        Excluye las eliminadas definitivamente (estado=-1).
        Devuelve solo id, nombre y estado. Sin paginación (máximo 10 sucursales por empresa).
    Parámetros:
        p_empresa_id : ID de la empresa
    ===============================================================================
    */

END$$

DELIMITER ;
