DROP PROCEDURE IF EXISTS sp_desactivar_producto;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_desactivar_producto`(
    IN p_producto_id BIGINT,
    IN p_empresa_id BIGINT
)
BEGIN

    DECLARE v_estado_actual TINYINT;

    -- Handler para errores SQL
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT
            'ERROR' AS estado,
            'Error al desactivar el producto' AS mensaje,
            0 AS id;
    END;

    START TRANSACTION;

    -- ============================================================
    -- VALIDAR QUE EXISTE Y PERTENECE A LA EMPRESA
    -- ============================================================

    SELECT estado
    INTO v_estado_actual
    FROM productos
    WHERE id = p_producto_id
    AND empresa_id = p_empresa_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT
            'ERROR_NO_EXISTE' AS estado,
            'El producto no existe' AS mensaje,
            0 AS id;

    ELSEIF v_estado_actual = 0 THEN
        -- Ya está desactivado
        ROLLBACK;
        SELECT
            'ERROR_YA_DESACTIVADO' AS estado,
            'El producto ya está desactivado' AS mensaje,
            p_producto_id AS id;

    ELSEIF v_estado_actual = -1 THEN
        -- Fue eliminado definitivamente, no se puede desactivar
        ROLLBACK;
        SELECT
            'ERROR_ELIMINADO' AS estado,
            'El producto fue eliminado definitivamente' AS mensaje,
            p_producto_id AS id;

    ELSE
        -- ============================================================
        -- DESACTIVAR PRODUCTO (solo desde estado=1)
        -- ============================================================

        UPDATE productos
        SET
            estado = 0,
            fecha_actualizacion = NOW()
        WHERE id = p_producto_id
        AND empresa_id = p_empresa_id;

        COMMIT;

        SELECT
            'OK' AS estado,
            'Producto desactivado correctamente' AS mensaje,
            p_producto_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_desactivar_producto
    Autor:
        Jose Cerron
    Descripción:
        Desactiva (cambia estado a 0) un producto.
        No lo elimina, solo lo marca como inactivo.
    Parámetros:
        p_producto_id: ID del producto a desactivar
        p_empresa_id: ID de la empresa propietaria (validación)
    Validaciones:
        - Producto existe
        - Producto pertenece a la empresa
        - Producto no está ya desactivado
        - Producto no fue eliminado definitivamente (estado=-1)
    Estados posibles:
        OK
        ERROR
        ERROR_NO_EXISTE
        ERROR_YA_DESACTIVADO
        ERROR_ELIMINADO
    Tablas involucradas:
        - productos
    ===============================================================================
    */

END$$

DELIMITER ;
