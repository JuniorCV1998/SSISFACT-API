ALTER TABLE configuracion_empresa
    ADD COLUMN asistente_nombre VARCHAR(30) NULL AFTER ticket_mensaje_pie,
    ADD COLUMN asistente_tono VARCHAR(20) NULL AFTER asistente_nombre;
