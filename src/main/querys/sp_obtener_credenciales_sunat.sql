DROP PROCEDURE IF EXISTS sp_obtener_credenciales_sunat;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_obtener_credenciales_sunat`(
    IN p_empresa_id BIGINT
)
BEGIN

    SELECT ruc, username_sunat, password_sunat
    FROM empresas
    WHERE id = p_empresa_id;

    /*
    ===============================================================================
    Nombre:
        sp_obtener_credenciales_sunat
    Descripción:
        Obtiene el RUC y las credenciales SUNAT (username/password encriptado)
        asociadas a una empresa, para construir el request al bot de SUNAT.
    ===============================================================================
    */

END$$

DELIMITER ;
