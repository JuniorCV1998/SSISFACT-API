ALTER TABLE configuracion_empresa
    ADD COLUMN whatsapp_mensaje_comprobante VARCHAR(255) NULL AFTER whatsapp_fecha_ultimo_envio;
