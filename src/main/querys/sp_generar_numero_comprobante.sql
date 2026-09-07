DROP PROCEDURE IF EXISTS sp_generar_numero_comprobante;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_generar_numero_comprobante`(
    IN p_empresa_id  BIGINT,
    IN p_sucursal_id BIGINT,
    IN p_tipo        VARCHAR(20)
)
BEGIN

    -- ===============================================================================
    -- IMPORTANTE — igual que sp_iniciar_venta, esta SP NO maneja su propia
    -- transacción: vive dentro de la transacción @Transactional de
    -- VentaServiceImpl para que el avance del correlativo quede atado al resto
    -- de la venta (si algo después falla, el correlativo también se revierte).
    -- ===============================================================================

    DECLARE v_serie_id BIGINT;
    DECLARE v_serie    VARCHAR(10);
    DECLARE v_numero   INT;

    -- Prioriza la serie de ESA sucursal; si el tipo todavía no está migrado a
    -- series por sucursal (ej. BOLETA/GUIA), cae a la serie empresa-wide
    -- (sucursal_id NULL).
    SELECT id, serie, correlativo_actual + 1
    INTO v_serie_id, v_serie, v_numero
    FROM series
    WHERE empresa_id = p_empresa_id
    AND (sucursal_id = p_sucursal_id OR sucursal_id IS NULL)
    AND tipo = p_tipo
    AND es_principal = 1
    AND estado = 1
    ORDER BY (sucursal_id IS NOT NULL) DESC
    LIMIT 1
    FOR UPDATE;

    IF v_serie_id IS NULL THEN
        SELECT 'ERROR_SERIE' AS estado,
               CONCAT('No hay una serie configurada para el tipo ', p_tipo) AS mensaje,
               NULL AS serie, 0 AS numero;

    ELSE
        UPDATE series SET correlativo_actual = v_numero WHERE id = v_serie_id;

        SELECT 'OK' AS estado, 'Número de comprobante generado' AS mensaje,
               v_serie AS serie, v_numero AS numero;
    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_generar_numero_comprobante
    Descripción:
        Avanza el correlativo de la serie principal de una sucursal (o de la
        empresa, para tipos aún no migrados a series por sucursal) para el
        tipo de comprobante indicado y devuelve serie+número a usar.
        El FOR UPDATE bloquea la fila de la serie hasta el commit de la venta,
        evitando que dos ventas concurrentes reciban el mismo número.
    Estados posibles:
        OK, ERROR_SERIE
    ===============================================================================
    */

END$$

DELIMITER ;
