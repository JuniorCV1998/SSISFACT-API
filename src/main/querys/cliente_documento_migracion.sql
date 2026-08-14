-- =========================================================================
-- CACHÉ LOCAL DNI/RUC (Decolecta) — tabla GLOBAL, sin empresa_id.
--
-- A propósito no es multi-tenant: los datos de RENIEC/SUNAT para un mismo
-- documento son idénticos sin importar qué empresa lo consulte, así que un
-- único caché compartido por toda la plataforma evita llamadas repetidas a
-- la API externa entre empresas distintas.
--
-- origen_datos indica de dónde salió el registro:
--   RENIEC -> vino de la consulta a /reniec/dni
--   SUNAT  -> vino de la consulta a /sunat/ruc
--   MANUAL -> lo tipeó un usuario a mano porque ni la BD ni la API lo tenían
-- =========================================================================

CREATE TABLE IF NOT EXISTS cliente_documento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cod_tipo_doc VARCHAR(2) NOT NULL COMMENT '01=DNI, 06=RUC',
    nro_documento VARCHAR(15) NOT NULL,
    nombres VARCHAR(100),               -- solo DNI
    apellido_paterno VARCHAR(32),        -- solo DNI
    apellido_materno VARCHAR(32),        -- solo DNI
    razon_social VARCHAR(120),           -- solo RUC
    estado_ruc VARCHAR(24),              -- solo RUC
    condicion VARCHAR(12),               -- solo RUC
    direccion VARCHAR(80),               -- solo RUC
    ubigeo VARCHAR(6),                   -- solo RUC
    origen_datos VARCHAR(12) NOT NULL COMMENT 'RENIEC|SUNAT|MANUAL',
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0=eliminado,1=activo',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_cliente_documento_doc UNIQUE (cod_tipo_doc, nro_documento)
);
