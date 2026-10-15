ALTER TABLE configuracion_empresa
    ADD COLUMN whatsapp_estado CHAR(1) NOT NULL DEFAULT 'N' AFTER color_acento,
    ADD COLUMN whatsapp_mensaje VARCHAR(500) NULL AFTER whatsapp_estado,
    ADD COLUMN whatsapp_hora_envio TIME NULL AFTER whatsapp_mensaje,
    ADD COLUMN whatsapp_fecha_ultimo_envio DATE NULL AFTER whatsapp_hora_envio;
