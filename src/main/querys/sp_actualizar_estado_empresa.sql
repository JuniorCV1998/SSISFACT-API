DROP PROCEDURE IF EXISTS sp_actualizar_estado_empresa;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_estado_empresa`(
    IN p_empresa_id BIGINT,
    IN p_estado     TINYINT
)
BEGIN

    DECLARE v_estado_actual TINYINT;
    DECLARE v_ruc           VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al actualizar el estado de la empresa' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    SELECT estado, ruc
    INTO v_estado_actual, v_ruc
    FROM empresas
    WHERE id = p_empresa_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_NO_EXISTE' AS estado, 'La empresa no existe' AS mensaje, 0 AS id;

    ELSEIF v_ruc = '00000000000' THEN
        ROLLBACK;
        SELECT 'ERROR_EMPRESA_PLATAFORMA' AS estado, 'No se puede modificar el estado de la empresa de la plataforma' AS mensaje, p_empresa_id AS id;

    ELSEIF p_estado NOT IN (0, 1, 2) THEN
        ROLLBACK;
        SELECT 'ERROR_ESTADO' AS estado, 'Estado inválido. Valores permitidos: 0 (eliminada), 1 (activa), 2 (pendiente)' AS mensaje, 0 AS id;

    ELSEIF v_estado_actual = p_estado THEN
        ROLLBACK;
        SELECT 'ERROR_MISMO_ESTADO' AS estado, 'La empresa ya está en ese estado' AS mensaje, p_empresa_id AS id;

    ELSE
        UPDATE empresas
        SET estado = p_estado, fecha_actualizacion = NOW()
        WHERE id = p_empresa_id;

        COMMIT;
        SELECT 'OK' AS estado, 'Estado de la empresa actualizado correctamente' AS mensaje, p_empresa_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_actualizar_estado_empresa
    Descripción:
        Panel superadmin: activa (1), suspende/marca pendiente (2) o elimina (0)
        una empresa. Protegida contra tocar la empresa fantasma de la plataforma.
    Estados posibles:
        OK, ERROR, ERROR_NO_EXISTE, ERROR_EMPRESA_PLATAFORMA, ERROR_ESTADO, ERROR_MISMO_ESTADO
    ===============================================================================
    */

END$$

DELIMITER ;
