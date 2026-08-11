DROP PROCEDURE IF EXISTS sp_actualizar_credenciales_sunat;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_credenciales_sunat`(
    IN p_empresa_id BIGINT,
    IN p_username_sunat VARCHAR(100),
    IN p_password_sunat VARCHAR(500)
)
BEGIN

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error en el proceso' AS mensaje, 0 AS id;
    END;

    IF NOT EXISTS (SELECT 1 FROM empresas WHERE id = p_empresa_id) THEN
        SELECT 'ERROR_EMPRESA' AS estado, 'La empresa no existe' AS mensaje, 0 AS id;

    ELSE
        START TRANSACTION;

        UPDATE empresas
        SET username_sunat = p_username_sunat,
            password_sunat = p_password_sunat
        WHERE id = p_empresa_id;

        COMMIT;
        SELECT 'OK' AS estado, 'Credenciales SUNAT actualizadas correctamente' AS mensaje, p_empresa_id AS id;
    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_actualizar_credenciales_sunat
    Descripción:
        Guarda/actualiza el usuario y password (ya encriptado por el backend)
        que se usarán para autenticar contra el bot de automatización SUNAT.
        El backend valida la conexión contra el bot ANTES de llamar a este SP.
    Estados posibles:
        OK, ERROR, ERROR_EMPRESA
    ===============================================================================
    */

END$$

DELIMITER ;
