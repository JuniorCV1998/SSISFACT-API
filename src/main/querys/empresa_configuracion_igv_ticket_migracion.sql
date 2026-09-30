ALTER TABLE configuracion_empresa
    ADD COLUMN igv_habilitado TINYINT NOT NULL DEFAULT 0 AFTER impuesto,
    ADD COLUMN ticket_mensaje_pie VARCHAR(255) NULL AFTER igv_habilitado;
