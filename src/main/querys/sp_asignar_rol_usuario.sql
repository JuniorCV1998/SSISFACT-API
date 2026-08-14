DROP PROCEDURE IF EXISTS sp_asignar_rol_usuario;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_asignar_rol_usuario`(
    IN p_usuario_id BIGINT,
    IN p_empresa_id BIGINT,
    IN p_rol        VARCHAR(50)
)
BEGIN

    DECLARE v_rol_id BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al asignar el rol' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    IF NOT EXISTS (
        SELECT 1 FROM usuarios WHERE id = p_usuario_id AND empresa_id = p_empresa_id
    ) THEN
        ROLLBACK;
        SELECT 'ERROR_USUARIO' AS estado, 'El usuario no existe o no pertenece a esa empresa' AS mensaje, 0 AS id;

    ELSEIF p_rol = 'SUPERADMIN' THEN
        ROLLBACK;
        SELECT 'ERROR_ROL_RESERVADO' AS estado, 'El rol SUPERADMIN no se puede asignar desde aquí' AS mensaje, 0 AS id;

    ELSE

        SELECT id INTO v_rol_id FROM roles WHERE nombre = p_rol LIMIT 1;

        IF v_rol_id IS NULL THEN
            ROLLBACK;
            SELECT 'ERROR_ROL' AS estado, 'El rol indicado no existe' AS mensaje, 0 AS id;

        ELSE
            DELETE FROM usuario_roles WHERE usuario_id = p_usuario_id;

            INSERT INTO usuario_roles (usuario_id, rol_id, fecha_creacion)
            VALUES (p_usuario_id, v_rol_id, NOW());

            COMMIT;
            SELECT 'OK' AS estado, CONCAT('Rol actualizado a ', p_rol) AS mensaje, p_usuario_id AS id;

        END IF;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_asignar_rol_usuario
    Descripción:
        Panel superadmin: reemplaza los roles de un usuario de una empresa por
        uno solo (ej. restaurar el rol ADMIN de un usuario que quedó bloqueado).
        No permite asignar SUPERADMIN (reservado a la cuenta de plataforma).
    Estados posibles:
        OK, ERROR, ERROR_USUARIO, ERROR_ROL_RESERVADO, ERROR_ROL
    ===============================================================================
    */

END$$

DELIMITER ;
