DROP PROCEDURE IF EXISTS sp_desactivar_categoria;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_desactivar_categoria`(
    IN p_categoria_id BIGINT,
    IN p_empresa_id BIGINT
)
BEGIN

    DECLARE v_estado_actual TINYINT;
    DECLARE v_productos_activos INT;

    -- Handler para errores SQL
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT
            'ERROR' AS estado,
            'Error al desactivar la categoría' AS mensaje,
            0 AS id;
    END;

    START TRANSACTION;

    -- ============================================================
    -- VALIDAR QUE EXISTE Y PERTENECE A LA EMPRESA
    -- ============================================================

    SELECT estado
    INTO v_estado_actual
    FROM categorias
    WHERE id = p_categoria_id
    AND empresa_id = p_empresa_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT
            'ERROR_NO_EXISTE' AS estado,
            'La categoría no existe' AS mensaje,
            0 AS id;

    ELSEIF v_estado_actual = 0 THEN
        -- Ya está desactivada
        ROLLBACK;
        SELECT
            'ERROR_YA_DESACTIVADA' AS estado,
            'La categoría ya está desactivada' AS mensaje,
            p_categoria_id AS id;

    ELSE
        -- ============================================================
        -- VALIDAR QUE NO TIENE PRODUCTOS ACTIVOS
        -- ============================================================

        SELECT COUNT(*)
        INTO v_productos_activos
        FROM productos
        WHERE categoria_id = p_categoria_id
        AND estado = 1;

        IF v_productos_activos > 0 THEN
            ROLLBACK;
            SELECT
                'ERROR_PRODUCTOS_ACTIVOS' AS estado,
                CONCAT('No se puede desactivar. La categoría tiene ',
                       v_productos_activos, ' producto(s) activo(s)') AS mensaje,
                p_categoria_id AS id;

        ELSE
            -- ============================================================
            -- DESACTIVAR CATEGORIA
            -- ============================================================

            UPDATE categorias
            SET
                estado = 0,
                fecha_actualizacion = NOW()
            WHERE id = p_categoria_id
            AND empresa_id = p_empresa_id;

            COMMIT;

            SELECT
                'OK' AS estado,
                'Categoría desactivada correctamente' AS mensaje,
                p_categoria_id AS id;

        END IF;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_desactivar_categoria
    Autor:
        Jose Cerron
    Descripción:
        Desactiva (cambia estado a 0) una categoría.
        No la elimina, solo la marca como inactiva.

        Validación: No permite desactivar si tiene productos activos.
    Parámetros:
        p_categoria_id: ID de la categoría a desactivar
        p_empresa_id: ID de la empresa propietaria (validación)
    Validaciones:
        - Categoría existe
        - Categoría pertenece a la empresa
        - Categoría no está ya desactivada
        - NO tiene productos activos asociados
    Estados posibles:
        OK
        ERROR
        ERROR_NO_EXISTE
        ERROR_YA_DESACTIVADA
        ERROR_PRODUCTOS_ACTIVOS
    Tablas involucradas:
        - categorias
        - productos (validación)
    ===============================================================================
    */

END$$

DELIMITER ;
