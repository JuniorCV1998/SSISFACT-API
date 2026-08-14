-- =========================================================================
-- DESCUENTOS EN VENTAS — soporte para descuento por ítem y descuento global
-- por venta. No idempotente (MySQL 8.0.40 no soporta ADD COLUMN IF NOT
-- EXISTS): si se vuelve a correr contra una BD que ya tiene estas columnas,
-- fallará con "Duplicate column name" — es esperado, no correrlo dos veces.
--
-- Semántica:
--   detalle_venta.subtotal   -> sigue siendo precio_unitario * cantidad (BRUTO,
--                                sin descuento, sin cambios respecto a antes).
--   detalle_venta.descuento  -> monto de descuento aplicado a esa línea.
--   ventas.subtotal / comprobantes.subtotal -> suma de subtotales brutos de
--                                items (sin cambios respecto a antes).
--   ventas.descuento / comprobantes.descuento -> descuento TOTAL de la venta
--                                = suma de descuentos por ítem + descuento
--                                global de la venta.
--   total = subtotal - descuento + impuestos
-- =========================================================================

ALTER TABLE detalle_venta
    ADD COLUMN descuento DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER precio_unitario;

ALTER TABLE ventas
    ADD COLUMN descuento DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER subtotal;

ALTER TABLE comprobantes
    ADD COLUMN descuento DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER subtotal;
