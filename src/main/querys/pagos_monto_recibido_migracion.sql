-- Soporta vuelto: cuánto recibió realmente el cajero (ej. en efectivo) frente a lo
-- que se aplicó a la venta (monto). El vuelto se calcula como monto_recibido - monto,
-- no se guarda aparte.
ALTER TABLE pagos ADD COLUMN monto_recibido DECIMAL(12,2) NULL AFTER monto;
