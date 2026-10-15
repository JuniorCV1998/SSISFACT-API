-- Generaliza el webhook de "Yape" a "pago digital" para que la misma infraestructura
-- (token, bandeja, match por saldo) sirva también para Plin. Se recrea limpio porque
-- la tabla/columnas anteriores (yape_webhook_migracion.sql) nunca llegaron a tener
-- datos reales (feature sin probar en vivo todavía).
ALTER TABLE configuracion_empresa
    CHANGE COLUMN yape_webhook_estado pago_digital_webhook_estado CHAR(1) NOT NULL DEFAULT 'N',
    CHANGE COLUMN yape_webhook_token_hash pago_digital_webhook_token_hash VARCHAR(64) NULL;

DROP TABLE yape_notificaciones;

CREATE TABLE pago_digital_notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    metodo VARCHAR(10) NOT NULL, -- YAPE, PLIN
    monto DECIMAL(10,2) NOT NULL,
    remitente VARCHAR(150) NULL,
    texto_original VARCHAR(500) NULL,
    fecha_recibido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- CONFIRMADA_AUTO, CONFIRMADA_MANUAL, SIN_MATCH, DESCARTADA
    estado VARCHAR(20) NOT NULL DEFAULT 'SIN_MATCH',
    venta_id BIGINT NULL,
    fecha_asociacion DATETIME NULL,
    CONSTRAINT fk_pago_digital_notif_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    CONSTRAINT fk_pago_digital_notif_venta FOREIGN KEY (venta_id) REFERENCES ventas(id)
);

CREATE INDEX idx_pago_digital_notif_empresa_estado ON pago_digital_notificaciones (empresa_id, estado);
