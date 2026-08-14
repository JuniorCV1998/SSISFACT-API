DROP PROCEDURE IF EXISTS sp_actualizar_plan_empresa;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_plan_empresa`(
    IN p_empresa_id        BIGINT,
    IN p_plan              VARCHAR(20),
    IN p_max_sucursal      INT,
    IN p_max_usuarios      INT,
    IN p_fecha_vencimiento DATE
)
BEGIN

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al actualizar el plan de la empresa' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM empresas WHERE id = p_empresa_id) THEN
        ROLLBACK;
        SELECT 'ERROR_NO_EXISTE' AS estado, 'La empresa no existe' AS mensaje, 0 AS id;

    ELSEIF p_max_sucursal < 0 OR p_max_usuarios < 0 THEN
        ROLLBACK;
        SELECT 'ERROR_LIMITES' AS estado, 'Los límites no pueden ser negativos' AS mensaje, 0 AS id;

    ELSEIF NOT EXISTS (SELECT 1 FROM configuracion_empresa WHERE empresa_id = p_empresa_id) THEN
        ROLLBACK;
        SELECT 'ERROR_SIN_CONFIGURACION' AS estado, 'La empresa no tiene configuración inicial' AS mensaje, 0 AS id;

    ELSE
        UPDATE configuracion_empresa
        SET plan = p_plan,
            max_sucursal = p_max_sucursal,
            max_usuarios = p_max_usuarios,
            fecha_vencimiento = p_fecha_vencimiento,
            fecha_actualizacion = NOW()
        WHERE empresa_id = p_empresa_id;

        COMMIT;
        SELECT 'OK' AS estado, 'Plan de la empresa actualizado correctamente' AS mensaje, p_empresa_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_actualizar_plan_empresa
    Descripción:
        Panel superadmin: actualiza plan, límites de sucursales/usuarios y fecha
        de vencimiento de membresía de una empresa (configuracion_empresa).
    Estados posibles:
        OK, ERROR, ERROR_NO_EXISTE, ERROR_LIMITES, ERROR_SIN_CONFIGURACION
    ===============================================================================
    */

END$$

DELIMITER ;
