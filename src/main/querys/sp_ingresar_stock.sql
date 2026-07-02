DROP PROCEDURE IF EXISTS sp_ingresar_stock;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_ingresar_stock`(
    IN p_producto_id BIGINT,
    IN p_sucursal_id BIGINT,
    IN p_cantidad INT,
    IN p_motivo VARCHAR(100),
    IN p_referencia_id BIGINT
)
BEGIN

    DECLARE v_inventario_id BIGINT;
    DECLARE v_stock_actual INT DEFAULT 0;
    DECLARE v_stock_nuevo INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;

        SELECT
            'ERROR' AS estado,
            'Error en el proceso' AS mensaje,
            0 AS id;
    END;

    START TRANSACTION;

    -- ============================================
    -- VALIDAR PRODUCTO
    -- ============================================

    IF NOT EXISTS (
        SELECT 1
        FROM productos
        WHERE id = p_producto_id
        AND estado = 1
    ) THEN

        ROLLBACK;

        SELECT
            'ERROR_PRODUCTO' AS estado,
            'Producto no encontrado' AS mensaje,
            0 AS id;

    -- ============================================
    -- VALIDAR SUCURSAL
    -- ============================================

    ELSEIF NOT EXISTS (
        SELECT 1
        FROM sucursales
        WHERE id = p_sucursal_id
        AND estado = 1
    ) THEN

        ROLLBACK;

        SELECT
            'ERROR_SUCURSAL' AS estado,
            'Sucursal no encontrada' AS mensaje,
            0 AS id;

    -- ============================================
    -- VALIDAR CANTIDAD
    -- ============================================

    ELSEIF p_cantidad <= 0 THEN

        ROLLBACK;

        SELECT
            'ERROR_CANTIDAD' AS estado,
            'La cantidad debe ser mayor a cero' AS mensaje,
            0 AS id;

    ELSE

        -- ============================================
        -- BUSCAR INVENTARIO
        -- ============================================

        SELECT id, stock
        INTO v_inventario_id, v_stock_actual
        FROM inventarios
        WHERE producto_id = p_producto_id
        AND sucursal_id = p_sucursal_id
        LIMIT 1;

        IF v_inventario_id IS NULL THEN

            INSERT INTO inventarios(
                producto_id,
                sucursal_id,
                stock,
                fecha_actualizacion
            )
            VALUES(
                p_producto_id,
                p_sucursal_id,
                p_cantidad,
                NOW()
            );

            SET v_inventario_id = LAST_INSERT_ID();
            SET v_stock_nuevo = p_cantidad;

        ELSE

            SET v_stock_nuevo = v_stock_actual + p_cantidad;

            UPDATE inventarios
            SET
                stock = v_stock_nuevo,
                fecha_actualizacion = NOW()
            WHERE id = v_inventario_id;

        END IF;

        -- ============================================
        -- REGISTRAR MOVIMIENTO
        -- ============================================

        INSERT INTO movimientos_stock(
            producto_id,
            sucursal_id,
            tipo,
            cantidad,
            motivo,
            referencia_id,
            fecha
        )
        VALUES(
            p_producto_id,
            p_sucursal_id,
            'ENTRADA',
            p_cantidad,
            p_motivo,
            p_referencia_id,
            NOW()
        );

        COMMIT;

        SELECT
            'OK' AS estado,
            CONCAT('Stock actualizado. Nuevo stock: ', v_stock_nuevo) AS mensaje,
            v_inventario_id AS id;

    END IF;

END$$

DELIMITER ;
