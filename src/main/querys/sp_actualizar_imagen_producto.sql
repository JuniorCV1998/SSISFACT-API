DROP PROCEDURE IF EXISTS sp_actualizar_imagen_producto;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_actualizar_imagen_producto`(
    IN p_producto_id BIGINT,
    IN p_empresa_id BIGINT,
    IN p_imagen_url VARCHAR(255)
)
BEGIN

    DECLARE v_estado_actual TINYINT;

    -- Handler para errores SQL
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT
            'ERROR' AS estado,
            'Error al actualizar la imagen del producto' AS mensaje,
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

    ELSEIF v_estado_actual = -1 THEN
        ROLLBACK;
        SELECT
            'ERROR_ELIMINADO' AS estado,
            'El producto fue eliminado definitivamente' AS mensaje,
            p_producto_id AS id;

    ELSE
        UPDATE productos
        SET
            imagen_url = p_imagen_url,
            fecha_actualizacion = NOW()
        WHERE id = p_producto_id
        AND empresa_id = p_empresa_id;

        COMMIT;

        SELECT
            'OK' AS estado,
            'Imagen actualizada correctamente' AS mensaje,
            p_producto_id AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_actualizar_imagen_producto
    Autor:
        Jose Cerron
    Descripción:
        Actualiza únicamente la imagen (imagen_url) de un producto, sin tocar
        el resto de sus campos.
    Parámetros:
        p_producto_id: ID del producto
        p_empresa_id: ID de la empresa propietaria (validación)
        p_imagen_url: URL pública de la imagen ya subida a R2
    Validaciones:
        - Producto existe
        - Producto pertenece a la empresa
        - Producto no fue eliminado definitivamente (estado=-1)
    Estados posibles:
        OK
        ERROR
        ERROR_NO_EXISTE
        ERROR_ELIMINADO
    Tablas involucradas:
        - productos
    ===============================================================================
    */

END$$

DELIMITER ;
