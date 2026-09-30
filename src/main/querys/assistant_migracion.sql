-- =========================================================================
-- Asistente "Fact": memoria persistente por empresa, log de conversaciones
-- y cola de aprendizaje procesada por el job programado. Todo multi-tenant
-- vía empresa_id. Tablas de tipo caché/log/cola sin validación de negocio,
-- por eso no llevan stored procedures (igual que cliente_documento).
-- =========================================================================

CREATE TABLE IF NOT EXISTS assistant_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL COMMENT 'ALIAS|PREFERENCIA|CORRECCION|CLIENTE_FRECUENTE',
    clave VARCHAR(150) NOT NULL,
    valor VARCHAR(500) NOT NULL,
    usos INT NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_assistant_memory_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT uq_memoria UNIQUE (empresa_id, tipo, clave),
    INDEX idx_memoria_empresa_usos (empresa_id, usos)
);

CREATE TABLE IF NOT EXISTS assistant_conversation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    rol VARCHAR(20) NOT NULL COMMENT 'user|assistant|tool',
    mensaje TEXT,
    tool_name VARCHAR(60),
    tool_args JSON,
    tool_result JSON,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assistant_log_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    INDEX idx_log_empresa_fecha (empresa_id, fecha_creacion),
    INDEX idx_log_empresa_usuario (empresa_id, usuario_id, fecha_creacion)
);

CREATE TABLE IF NOT EXISTS assistant_learning_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    tipo_evento VARCHAR(30) NOT NULL COMMENT 'CORRECCION|PATRON_DETECTADO',
    payload JSON NOT NULL,
    procesado TINYINT NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_procesado TIMESTAMP NULL,
    CONSTRAINT fk_assistant_queue_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    INDEX idx_queue_pendientes (procesado, fecha_creacion),
    INDEX idx_queue_empresa (empresa_id)
);
