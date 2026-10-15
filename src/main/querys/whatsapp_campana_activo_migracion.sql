-- whatsapp_estado refleja solo la CONEXIÓN (vinculado o no). Se separa un toggle propio
-- para el mensaje programado diario, porque el admin puede querer mantener WhatsApp
-- vinculado (para el envío de venta con un clic) sin que el recordatorio automático
-- esté activo.
ALTER TABLE configuracion_empresa
    ADD COLUMN whatsapp_campana_activo TINYINT NOT NULL DEFAULT 0 AFTER whatsapp_estado;
