-- Centro de notificaciones del sistema (campana en el frontend). Genérico: no es
-- solo para pagos digitales — "tipo" identifica cada clase de evento (hoy
-- PAGO_DIGITAL_CONFIRMADO; a futuro cualquier otra cosa relevante para el usuario).
--
-- usuario_id NULL = notificación para toda la empresa (cualquier usuario logueado
-- la ve, ej. "se confirmó un pago"); usuario_id con valor = dirigida a una persona
-- puntual (ej. una tarea asignada). entidad_tipo/entidad_id permiten que el front
-- navegue al recurso relacionado (ej. "VENTA" / 93) sin tener que parsear el mensaje.
CREATE TABLE notificaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    tipo VARCHAR(40) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensaje VARCHAR(500) NULL,
    entidad_tipo VARCHAR(40) NULL,
    entidad_id BIGINT NULL,
    leida CHAR(1) NOT NULL DEFAULT 'N',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_leida DATETIME NULL,
    CONSTRAINT fk_notificacion_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Listado típico: de una empresa (+ opcionalmente de un usuario puntual), no leídas primero, más recientes primero.
CREATE INDEX idx_notificaciones_empresa_leida ON notificaciones (empresa_id, leida, fecha_creacion);
CREATE INDEX idx_notificaciones_usuario ON notificaciones (usuario_id, leida);
