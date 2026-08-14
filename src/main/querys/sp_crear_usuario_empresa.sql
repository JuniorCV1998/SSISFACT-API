DROP PROCEDURE IF EXISTS sp_crear_usuario_empresa;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_usuario_empresa`(
    IN p_empresa_id       BIGINT,
    IN p_nombre           VARCHAR(150),
    IN p_email            VARCHAR(100),
    IN p_documento        VARCHAR(12),
    IN p_contrasena_hash  VARCHAR(255),
    IN p_rol              VARCHAR(50),
    IN p_sucursal_id      BIGINT
)
BEGIN

    DECLARE v_rol_id  BIGINT;
    DECLARE v_rol_tipo VARCHAR(10);
    DECLARE v_usuario_id BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al crear el usuario' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    SELECT id, tipo INTO v_rol_id, v_rol_tipo FROM roles WHERE nombre = p_rol LIMIT 1;

    IF v_rol_id IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_ROL' AS estado, 'El rol indicado no existe' AS mensaje, 0 AS id;

    ELSEIF v_rol_tipo IS NULL OR v_rol_tipo <> 'USER' THEN
        ROLLBACK;
        SELECT 'ERROR_ROL_NO_PERMITIDO' AS estado,
               'Solo puedes asignar roles de tipo trabajador (CAJERO, SUPERVISOR, ALMACEN, AUDITOR)' AS mensaje,
               0 AS id;

    ELSEIF EXISTS (SELECT 1 FROM usuarios WHERE email = p_email) THEN
        ROLLBACK;
        SELECT 'ERROR_EMAIL_EXISTE' AS estado, 'Ya existe un usuario con ese email' AS mensaje, 0 AS id;

    ELSEIF EXISTS (SELECT 1 FROM usuarios WHERE documento = p_documento) THEN
        ROLLBACK;
        SELECT 'ERROR_DOCUMENTO_EXISTE' AS estado, 'Ya existe un usuario con ese documento' AS mensaje, 0 AS id;

    ELSEIF p_sucursal_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM sucursales WHERE id = p_sucursal_id AND empresa_id = p_empresa_id AND estado = 1
    ) THEN
        ROLLBACK;
        SELECT 'ERROR_SUCURSAL' AS estado, 'La sucursal no existe o no pertenece a esa empresa' AS mensaje, 0 AS id;

    ELSE

        INSERT INTO usuarios (empresa_id, nombre, email, documento, contrasena, sucursal_id, estado)
        VALUES (p_empresa_id, p_nombre, p_email, p_documento, p_contrasena_hash, p_sucursal_id, 1);

        SET v_usuario_id = LAST_INSERT_ID();

        INSERT INTO usuario_roles (usuario_id, rol_id, fecha_creacion)
        VALUES (v_usuario_id, v_rol_id, NOW());

        COMMIT;
        SELECT 'OK' AS estado, 'Trabajador creado correctamente' AS mensaje, v_usuario_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_crear_usuario_empresa
    Descripción:
        Crea un trabajador dentro de la empresa del admin logueado, con su rol
        y (opcionalmente) su sucursal asignados en el mismo paso. Solo permite
        asignar roles de tipo 'USER' (CAJERO, SUPERVISOR, ALMACEN, AUDITOR) —
        roles de tipo 'ADMIN' o 'SYSTEM' están reservados al superadmin de la
        plataforma (ver sp_asignar_rol_usuario). email y documento son únicos
        en toda la plataforma (no solo dentro de la empresa).
    Estados posibles:
        OK, ERROR_ROL, ERROR_ROL_NO_PERMITIDO, ERROR_EMAIL_EXISTE,
        ERROR_DOCUMENTO_EXISTE, ERROR_SUCURSAL
    ===============================================================================
    */

END$$

DELIMITER ;
