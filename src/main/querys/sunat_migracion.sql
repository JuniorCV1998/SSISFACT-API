-- ===============================================================================
-- Migración: integración SUNAT
-- Agrega credenciales SUNAT a empresas y crea tablas de histórico de
-- notificaciones/mensajes. Ejecutar una sola vez sobre la base existente.
-- ===============================================================================

ALTER TABLE empresas
    ADD COLUMN username_sunat VARCHAR(100) NULL AFTER estado,
    ADD COLUMN password_sunat VARCHAR(500) NULL AFTER username_sunat;

-- =========================
-- SUNAT_NOTIFICACIONES
-- =========================
CREATE TABLE IF NOT EXISTS sunat_notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    ruc VARCHAR(20),
    sunat_id VARCHAR(50) NOT NULL,
    asunto VARCHAR(1000),
    fecha_publicacion VARCHAR(100),
    categoria_codigo VARCHAR(50),
    categoria_descripcion VARCHAR(500),
    leido TINYINT(1) NOT NULL DEFAULT 0,
    destacado TINYINT(1) NOT NULL DEFAULT 0,
    urgente TINYINT(1) NOT NULL DEFAULT 0,
    tiene_adjunto TINYINT(1) NOT NULL DEFAULT 0,
    fecha_ultima_sincronizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_sunat_notif_empresa_sunat_id (empresa_id, sunat_id),
    CONSTRAINT fk_sunat_notif_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);
CREATE INDEX idx_sunat_notif_empresa ON sunat_notificaciones(empresa_id);

-- =========================
-- SUNAT_MENSAJES
-- =========================
CREATE TABLE IF NOT EXISTS sunat_mensajes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    ruc VARCHAR(20),
    sunat_id VARCHAR(50) NOT NULL,
    asunto VARCHAR(1000),
    mensaje VARCHAR(4000),
    remitente VARCHAR(500),
    fecha_publicacion VARCHAR(100),
    leido TINYINT(1) NOT NULL DEFAULT 0,
    tiene_adjunto TINYINT(1) NOT NULL DEFAULT 0,
    fecha_ultima_sincronizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_sunat_mensaje_empresa_sunat_id (empresa_id, sunat_id),
    CONSTRAINT fk_sunat_mensaje_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);
CREATE INDEX idx_sunat_mensaje_empresa ON sunat_mensajes(empresa_id);
