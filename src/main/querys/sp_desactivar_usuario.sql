DROP PROCEDURE IF EXISTS sp_desactivar_usuario;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_desactivar_usuario`(
    IN p_usuario_id BIGINT,
    IN p_empresa_id BIGINT
)
BEGIN

    DECLARE v_estado_actual TINYINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al desactivar el usuario' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    SELECT estado
    INTO v_estado_actual
    FROM usuarios
    WHERE id = p_usuario_id
    AND empresa_id = p_empresa_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_USUARIO' AS estado, 'El usuario no existe o no pertenece a esa empresa' AS mensaje, 0 AS id;

    ELSEIF v_estado_actual = 0 THEN
        ROLLBACK;
        SELECT 'ERROR_YA_DESACTIVADO' AS estado, 'El usuario ya está desactivado' AS mensaje, p_usuario_id AS id;

    ELSE
        UPDATE usuarios
        SET estado = 0, fecha_actualizacion = NOW()
        WHERE id = p_usuario_id;

        COMMIT;
        SELECT 'OK' AS estado, 'Usuario desactivado correctamente' AS mensaje, p_usuario_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_desactivar_usuario
    Descripción:
        Desactiva (estado=0) a un trabajador de la empresa. Bloquea su próximo
        login (ver AuthController.login) sin borrar sus datos ni su historial.
    Parámetros:
        p_usuario_id: ID del trabajador a desactivar
        p_empresa_id: ID de la empresa del admin que ejecuta la acción (ownership)
    Estados posibles:
        OK, ERROR, ERROR_USUARIO, ERROR_YA_DESACTIVADO
    Tablas involucradas:
        - usuarios
    ===============================================================================
    */

END$$

DELIMITER ;
