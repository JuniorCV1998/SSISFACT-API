ALTER TABLE configuracion_empresa
    ADD COLUMN color_primario VARCHAR(7) NULL AFTER voz_genero,
    ADD COLUMN color_secundario VARCHAR(7) NULL AFTER color_primario,
    ADD COLUMN color_acento VARCHAR(7) NULL AFTER color_secundario;
