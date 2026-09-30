DROP PROCEDURE IF EXISTS sp_activar_usuario;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_activar_usuario`(
    IN p_usuario_id BIGINT,
    IN p_empresa_id BIGINT
)
BEGIN

    DECLARE v_estado_actual TINYINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al activar el usuario' AS mensaje, 0 AS id;
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

    ELSEIF v_estado_actual = 1 THEN
        ROLLBACK;
        SELECT 'ERROR_YA_ACTIVO' AS estado, 'El usuario ya está activo' AS mensaje, p_usuario_id AS id;

    ELSE
        UPDATE usuarios
        SET estado = 1, fecha_actualizacion = NOW()
        WHERE id = p_usuario_id;

        COMMIT;
        SELECT 'OK' AS estado, 'Usuario activado correctamente' AS mensaje, p_usuario_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_activar_usuario
    Descripción:
        Reactiva (estado=1) a un trabajador previamente desactivado, restaurando
        su acceso de login.
    Parámetros:
        p_usuario_id: ID del trabajador a activar
        p_empresa_id: ID de la empresa del admin que ejecuta la acción (ownership)
    Estados posibles:
        OK, ERROR, ERROR_USUARIO, ERROR_YA_ACTIVO
    Tablas involucradas:
        - usuarios
    ===============================================================================
    */

END$$

DELIMITER ;
