-- Renombra el método de pago "PLIN" a "PLIN INTERBANK" (más específico: distingue
-- que la notificación viene de la app Interbank). Se hace en 2 pasos sobre "pagos"
-- porque es ENUM: primero se agrega el nuevo valor sin quitar el viejo, se migran
-- los datos históricos, y recién ahí se quita "PLIN" del ENUM.

-- 1. pagos.metodo (ENUM) — agregar el nuevo valor manteniendo el viejo
ALTER TABLE pagos
    MODIFY COLUMN metodo ENUM('EFECTIVO','YAPE','PLIN','PLIN INTERBANK','TRANSFERENCIA','TARJETA') NOT NULL;

-- 2. Migrar histórico de pagos
UPDATE pagos SET metodo = 'PLIN INTERBANK' WHERE metodo = 'PLIN';

-- 3. Quitar "PLIN" del ENUM (ya no debe quedar ninguna fila con ese valor)
ALTER TABLE pagos
    MODIFY COLUMN metodo ENUM('EFECTIVO','YAPE','PLIN INTERBANK','TRANSFERENCIA','TARJETA') NOT NULL;

-- 4. pago_digital_notificaciones.metodo (VARCHAR(10)) — "PLIN INTERBANK" (14) no entra,
--    se ensancha antes de migrar los datos históricos.
ALTER TABLE pago_digital_notificaciones
    MODIFY COLUMN metodo VARCHAR(20) NOT NULL;

UPDATE pago_digital_notificaciones SET metodo = 'PLIN INTERBANK' WHERE metodo = 'PLIN';
