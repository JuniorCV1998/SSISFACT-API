DROP PROCEDURE IF EXISTS sp_asignar_sucursal_usuario;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_asignar_sucursal_usuario`(
    IN p_usuario_id  BIGINT,
    IN p_empresa_id  BIGINT,
    IN p_sucursal_id BIGINT
)
BEGIN

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al asignar la sucursal' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    IF NOT EXISTS (
        SELECT 1 FROM usuarios WHERE id = p_usuario_id AND empresa_id = p_empresa_id
    ) THEN
        ROLLBACK;
        SELECT 'ERROR_USUARIO' AS estado, 'El usuario no existe o no pertenece a esa empresa' AS mensaje, 0 AS id;

    ELSEIF p_sucursal_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM sucursales WHERE id = p_sucursal_id AND empresa_id = p_empresa_id AND estado = 1
    ) THEN
        ROLLBACK;
        SELECT 'ERROR_SUCURSAL' AS estado, 'La sucursal no existe o no pertenece a esa empresa' AS mensaje, 0 AS id;

    ELSE
        UPDATE usuarios SET sucursal_id = p_sucursal_id WHERE id = p_usuario_id;

        COMMIT;
        SELECT 'OK' AS estado, 'Sucursal asignada correctamente' AS mensaje, p_usuario_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_asignar_sucursal_usuario
    Descripción:
        Asigna (o quita, si p_sucursal_id es NULL) la sucursal fija de un
        trabajador de la empresa. Un usuario con ROLE_ADMIN no depende de este
        campo (queda libre de operar en cualquier sucursal vía SucursalAccessGuard
        en el backend); para el resto de roles, este valor es el que limita en
        qué sucursal pueden abrir caja / vender / manejar stock.
    Estados posibles:
        OK, ERROR_USUARIO, ERROR_SUCURSAL
    ===============================================================================
    */

END$$

DELIMITER ;
