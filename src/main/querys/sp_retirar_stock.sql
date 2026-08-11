DROP PROCEDURE IF EXISTS sp_retirar_stock;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_retirar_stock`(
    IN p_producto_id   BIGINT,
    IN p_sucursal_id   BIGINT,
    IN p_cantidad      INT,
    IN p_tipo          VARCHAR(20),
    IN p_motivo        VARCHAR(100),
    IN p_referencia_id BIGINT
)
BEGIN

    DECLARE v_inventario_id BIGINT;
    DECLARE v_stock_actual  INT;
    DECLARE v_stock_nuevo   INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error en el proceso' AS mensaje, 0 AS stock;
    END;

    START TRANSACTION;

    -- ============================================================
    -- VALIDAR TIPO
    -- ============================================================

    IF p_tipo IS NULL OR p_tipo NOT IN ('VENTA', 'MERMA', 'AJUSTE') THEN
        ROLLBACK;
        SELECT
            'ERROR_TIPO' AS estado,
            'Tipo inválido. Valores permitidos: VENTA, MERMA, AJUSTE' AS mensaje,
            0 AS stock;

    -- ============================================================
    -- VALIDAR PRODUCTO
    -- ============================================================

    ELSEIF NOT EXISTS (
        SELECT 1 FROM productos WHERE id = p_producto_id AND estado = 1
    ) THEN
        ROLLBACK;
        SELECT 'ERROR_PRODUCTO' AS estado, 'Producto no encontrado' AS mensaje, 0 AS stock;

    -- ============================================================
    -- VALIDAR SUCURSAL
    -- ============================================================

    ELSEIF NOT EXISTS (
        SELECT 1 FROM sucursales WHERE id = p_sucursal_id AND estado = 1
    ) THEN
        ROLLBACK;
        SELECT 'ERROR_SUCURSAL' AS estado, 'Sucursal no encontrada' AS mensaje, 0 AS stock;

    -- ============================================================
    -- VALIDAR CANTIDAD
    -- ============================================================

    ELSEIF p_cantidad <= 0 THEN
        ROLLBACK;
        SELECT 'ERROR_CANTIDAD' AS estado, 'La cantidad debe ser mayor a cero' AS mensaje, 0 AS stock;

    ELSE

        -- ============================================================
        -- OBTENER INVENTARIO
        -- ============================================================

        SELECT id, stock
        INTO v_inventario_id, v_stock_actual
        FROM inventarios
        WHERE producto_id = p_producto_id
          AND sucursal_id = p_sucursal_id
        LIMIT 1;

        IF v_inventario_id IS NULL THEN
            ROLLBACK;
            SELECT
                'ERROR_INVENTARIO' AS estado,
                'No existe inventario para el producto en esta sucursal' AS mensaje,
                0 AS stock;

        -- ============================================================
        -- VALIDAR STOCK DISPONIBLE
        -- ============================================================

        ELSEIF v_stock_actual < p_cantidad THEN
            ROLLBACK;
            SELECT
                'ERROR_STOCK_INSUFICIENTE' AS estado,
                CONCAT('Stock disponible: ', v_stock_actual, '. Cantidad solicitada: ', p_cantidad) AS mensaje,
                v_stock_actual AS stock;

        ELSE

            SET v_stock_nuevo = v_stock_actual - p_cantidad;

            UPDATE inventarios
            SET stock = v_stock_nuevo, fecha_actualizacion = NOW()
            WHERE id = v_inventario_id;

            INSERT INTO movimientos_stock (
                producto_id, sucursal_id, tipo, operacion, cantidad, motivo, referencia_id, fecha
            ) VALUES (
                p_producto_id, p_sucursal_id, p_tipo, 'SALIDA', p_cantidad, p_motivo, p_referencia_id, NOW()
            );

            COMMIT;

            SELECT
                'OK' AS estado,
                CONCAT('Retiro registrado correctamente. Stock actual: ', v_stock_nuevo) AS mensaje,
                v_stock_nuevo AS stock;

        END IF;

    END IF;

END$$

DELIMITER ;
