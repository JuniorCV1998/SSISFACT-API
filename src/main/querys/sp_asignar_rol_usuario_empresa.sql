DROP PROCEDURE IF EXISTS sp_asignar_rol_usuario_empresa;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_asignar_rol_usuario_empresa`(
    IN p_usuario_id BIGINT,
    IN p_empresa_id BIGINT,
    IN p_rol        VARCHAR(50)
)
BEGIN

    DECLARE v_rol_id  BIGINT;
    DECLARE v_rol_tipo VARCHAR(10);

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

    ELSE

        SELECT id, tipo INTO v_rol_id, v_rol_tipo FROM roles WHERE nombre = p_rol LIMIT 1;

        IF v_rol_id IS NULL THEN
            ROLLBACK;
            SELECT 'ERROR_ROL' AS estado, 'El rol indicado no existe' AS mensaje, 0 AS id;

        ELSEIF v_rol_tipo IS NULL OR v_rol_tipo <> 'USER' THEN
            ROLLBACK;
            SELECT 'ERROR_ROL_NO_PERMITIDO' AS estado,
                   'Solo puedes asignar roles de tipo trabajador (CAJERO, SUPERVISOR, ALMACEN, AUDITOR)' AS mensaje,
                   0 AS id;

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
        sp_asignar_rol_usuario_empresa
    Descripción:
        Reemplaza el rol de un trabajador de la PROPIA empresa del admin
        logueado. A diferencia de sp_asignar_rol_usuario (uso exclusivo del
        superadmin de la plataforma, sin restricción de tipo salvo bloquear
        'SUPERADMIN'), este SP solo permite asignar roles de tipo 'USER' —
        un admin de empresa nunca puede promoverse a sí mismo ni a otro
        usuario a ADMIN/SUNAT/SUPERADMIN desde aquí.
    Estados posibles:
        OK, ERROR_USUARIO, ERROR_ROL, ERROR_ROL_NO_PERMITIDO
    ===============================================================================
    */

END$$

DELIMITER ;
