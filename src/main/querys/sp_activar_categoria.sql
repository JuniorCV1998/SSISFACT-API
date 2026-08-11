DROP PROCEDURE IF EXISTS sp_activar_categoria;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_activar_categoria`(
    IN p_categoria_id BIGINT,
    IN p_empresa_id   BIGINT
)
BEGIN

    DECLARE v_estado_actual TINYINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al activar la categoría' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    SELECT estado
    INTO v_estado_actual
    FROM categorias
    WHERE id = p_categoria_id
    AND empresa_id = p_empresa_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_NO_EXISTE' AS estado, 'La categoría no existe' AS mensaje, 0 AS id;

    ELSEIF v_estado_actual = 1 THEN
        ROLLBACK;
        SELECT 'ERROR_YA_ACTIVA' AS estado, 'La categoría ya está activa' AS mensaje, p_categoria_id AS id;

    ELSEIF v_estado_actual = -1 THEN
        ROLLBACK;
        SELECT 'ERROR_ELIMINADA' AS estado, 'La categoría fue eliminada definitivamente y no puede ser reactivada' AS mensaje, p_categoria_id AS id;

    ELSE
        UPDATE categorias
        SET estado = 1, fecha_actualizacion = NOW()
        WHERE id = p_categoria_id AND empresa_id = p_empresa_id;

        COMMIT;
        SELECT 'OK' AS estado, 'Categoría activada correctamente' AS mensaje, p_categoria_id AS id;
    END IF;

    /*
    ===============================================================================
    Estados posibles: OK, ERROR, ERROR_NO_EXISTE, ERROR_YA_ACTIVA, ERROR_ELIMINADA
    Estados de categoría: 1=Activa, 0=Inactiva, -1=Eliminada definitivamente
    ===============================================================================
    */

END$$

DELIMITER ;
