DROP PROCEDURE IF EXISTS sp_iniciar_venta;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_iniciar_venta`(
    IN p_empresa_id  BIGINT,
    IN p_sucursal_id BIGINT,
    IN p_cliente_id  BIGINT,
    IN p_usuario_id  BIGINT
)
BEGIN

    -- ===============================================================================
    -- IMPORTANTE — esta SP NO maneja su propia transacción (sin START TRANSACTION /
    -- COMMIT / ROLLBACK ni EXIT HANDLER), a diferencia del resto de SPs del proyecto.
    -- Se ejecuta dentro de la transacción @Transactional de VentaServiceImpl junto
    -- con el resto del registro de la venta (detalle, stock, comprobante, pagos):
    -- si hiciera su propio COMMIT/ROLLBACK aquí, confirmaría/abortaría también todo
    -- lo que ya se hizo antes en esa misma conexión, rompiendo la atomicidad de la
    -- venta completa. Un SQLEXCEPTION real aquí se propaga tal cual a Java y hace
    -- rollback de toda la venta, igual que cualquier otro fallo del flujo.
    -- ===============================================================================

    DECLARE v_caja_id      BIGINT;
    DECLARE v_cliente_id   BIGINT;
    DECLARE v_venta_id     BIGINT;

    IF NOT EXISTS (
        SELECT 1 FROM sucursales WHERE id = p_sucursal_id AND empresa_id = p_empresa_id AND estado = 1
    ) THEN
        SELECT 'ERROR_SUCURSAL' AS estado, 'Sucursal no encontrada' AS mensaje, 0 AS id, 0 AS caja_id, 0 AS cliente_id;

    ELSE

        SELECT id INTO v_caja_id
        FROM cajas
        WHERE usuario_id = p_usuario_id
        AND sucursal_id = p_sucursal_id
        AND estado = 'ABIERTA'
        LIMIT 1;

        IF v_caja_id IS NULL THEN
            SELECT 'ERROR_CAJA_NO_ABIERTA' AS estado, 'Debes abrir una caja en esta sucursal antes de vender' AS mensaje, 0 AS id, 0 AS caja_id, 0 AS cliente_id;

        ELSE

            IF p_cliente_id IS NULL THEN
                SELECT id INTO v_cliente_id
                FROM clientes
                WHERE empresa_id = p_empresa_id AND numero_documento = '00000000'
                LIMIT 1;
            ELSEIF EXISTS (
                SELECT 1 FROM clientes WHERE id = p_cliente_id AND empresa_id = p_empresa_id AND estado = 1
            ) THEN
                SET v_cliente_id = p_cliente_id;
            END IF;

            IF v_cliente_id IS NULL THEN
                SELECT 'ERROR_CLIENTE' AS estado, 'El cliente no existe o no pertenece a la empresa' AS mensaje, 0 AS id, 0 AS caja_id, 0 AS cliente_id;

            ELSE
                INSERT INTO ventas (
                    empresa_id, sucursal_id, cliente_id, usuario_id, caja_id,
                    subtotal, impuestos, total, estado, fecha
                ) VALUES (
                    p_empresa_id, p_sucursal_id, v_cliente_id, p_usuario_id, v_caja_id,
                    0, 0, 0, 'PENDIENTE', NOW()
                );

                SET v_venta_id = LAST_INSERT_ID();

                SELECT 'OK' AS estado, 'Venta iniciada correctamente' AS mensaje,
                       v_venta_id AS id, v_caja_id AS caja_id, v_cliente_id AS cliente_id;
            END IF;

        END IF;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_iniciar_venta
    Descripción:
        Crea la cabecera de una venta (estado PENDIENTE, totales en 0) resolviendo
        la caja abierta del usuario en la sucursal y el cliente (o el cliente
        genérico "CLIENTE VARIOS" si no se especifica uno). Los ítems, el stock,
        el comprobante y los pagos se agregan después, en la misma transacción,
        desde VentaServiceImpl.
    Estados posibles:
        OK, ERROR_SUCURSAL, ERROR_CAJA_NO_ABIERTA, ERROR_CLIENTE
    ===============================================================================
    */

END$$

DELIMITER ;
