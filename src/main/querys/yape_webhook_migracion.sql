-- Webhook de pagos por Yape: un celular con app de automatización (MacroDroid/Tasker)
-- avisa al backend cuando llega una notificación de Yape, autenticándose con un token
-- propio de la empresa (no JWT de usuario). El token se guarda hasheado (SHA-256);
-- el valor en claro solo se muestra una vez al generarlo (igual que una API key).
ALTER TABLE configuracion_empresa
    ADD COLUMN yape_webhook_estado CHAR(1) NOT NULL DEFAULT 'N' AFTER whatsapp_mensaje_comprobante,
    ADD COLUMN yape_webhook_token_hash VARCHAR(64) NULL AFTER yape_webhook_estado;

-- Bandeja de notificaciones recibidas: todas se guardan (incluso las que no logran
-- asociarse a ninguna venta) para auditoría y para que el ADMIN las asocie a mano.
CREATE TABLE yape_notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    remitente VARCHAR(150) NULL,
    texto_original VARCHAR(500) NULL,
    fecha_recibido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- CONFIRMADA_AUTO, CONFIRMADA_MANUAL, SIN_MATCH, DESCARTADA
    estado VARCHAR(20) NOT NULL DEFAULT 'SIN_MATCH',
    venta_id BIGINT NULL,
    fecha_asociacion DATETIME NULL,
    CONSTRAINT fk_yape_notif_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    CONSTRAINT fk_yape_notif_venta FOREIGN KEY (venta_id) REFERENCES ventas(id)
);

CREATE INDEX idx_yape_notif_empresa_estado ON yape_notificaciones (empresa_id, estado);
