-- =========================================================================
-- MODULO DE VENTAS (control interno) — Clientes, Cajas, Series, Ventas,
-- Detalle de venta, Comprobantes (boleta/guía simples) y Pagos.
--
-- Sin envío a SUNAT todavía: comprobantes.estado_sunat siempre queda en
-- 'PENDIENTE' en esta fase. tipo incluye 'GUIA' (guía de remisión simple,
-- sin valor tributario) además de 'BOLETA'.
--
-- NOTA: en este entorno las tablas clientes/cajas/series/ventas/detalle_venta/
-- comprobantes/pagos ya existían (creadas antes con el script de diseño), por
-- lo que los CREATE TABLE de abajo son no-op (IF NOT EXISTS) y lo que de
-- verdad aplica son los ALTER TABLE para agregar 'GUIA' a los ENUM.
-- =========================================================================

-- =========================
-- CLIENTES
-- =========================
CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    tipo_documento ENUM('DNI','RUC','CE','PASAPORTE') NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(255),
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0=eliminado,1=activo',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT uq_cliente_documento UNIQUE (empresa_id, tipo_documento, numero_documento)
);
CREATE INDEX idx_cliente_empresa ON clientes(empresa_id);
CREATE INDEX idx_cliente_nombre ON clientes(nombre);

-- =========================
-- CAJAS
-- =========================
CREATE TABLE IF NOT EXISTS cajas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sucursal_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    monto_inicial DECIMAL(12,2) NOT NULL,
    monto_final DECIMAL(12,2),
    fecha_apertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP NULL,
    estado ENUM('ABIERTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
    CONSTRAINT fk_caja_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id),
    CONSTRAINT fk_caja_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
CREATE INDEX idx_caja_sucursal ON cajas(sucursal_id);
CREATE INDEX idx_caja_usuario ON cajas(usuario_id);
CREATE INDEX idx_caja_estado ON cajas(estado);

-- =========================
-- SERIES (numeración de comprobantes)
-- =========================
CREATE TABLE IF NOT EXISTS series (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    tipo ENUM('BOLETA','GUIA','FACTURA','NOTA_CREDITO','NOTA_DEBITO') NOT NULL,
    serie VARCHAR(10) NOT NULL,
    correlativo_actual INT NOT NULL DEFAULT 0,
    es_principal TINYINT NOT NULL DEFAULT 1 COMMENT '1=principal',
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0=inactivo,1=activo',
    UNIQUE (empresa_id, tipo, serie),
    CONSTRAINT fk_series_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);
CREATE INDEX idx_series_empresa_tipo ON series(empresa_id, tipo);
CREATE INDEX idx_series_principal ON series(empresa_id, tipo, es_principal);

-- =========================
-- VENTAS
-- =========================
CREATE TABLE IF NOT EXISTS ventas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    cliente_id BIGINT,
    usuario_id BIGINT NOT NULL,
    caja_id BIGINT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    impuestos DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Total de la venta realizada',
    estado ENUM('PENDIENTE','PAGADA','ANULADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_venta_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT fk_venta_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_venta_caja FOREIGN KEY (caja_id) REFERENCES cajas(id)
);
CREATE INDEX idx_venta_empresa ON ventas(empresa_id);
CREATE INDEX idx_venta_sucursal ON ventas(sucursal_id);
CREATE INDEX idx_venta_usuario ON ventas(usuario_id);
CREATE INDEX idx_venta_caja ON ventas(caja_id);
CREATE INDEX idx_venta_fecha ON ventas(fecha);

-- =========================
-- DETALLE VENTA
-- =========================
CREATE TABLE IF NOT EXISTS detalle_venta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_venta FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_venta_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
);
CREATE INDEX idx_detalle_venta ON detalle_venta(venta_id);
CREATE INDEX idx_detalle_producto ON detalle_venta(producto_id);

-- =========================
-- COMPROBANTES (boleta/guía simples — sin valor tributario por ahora)
-- =========================
CREATE TABLE IF NOT EXISTS comprobantes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    venta_id BIGINT NOT NULL,
    tipo ENUM('BOLETA','GUIA','FACTURA','NOTA_CREDITO','NOTA_DEBITO') NOT NULL,
    serie VARCHAR(10) NOT NULL,
    numero INT NOT NULL,
    cliente_nombre VARCHAR(150),
    cliente_documento VARCHAR(20),
    subtotal DECIMAL(12,2) NOT NULL,
    impuestos DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    estado_sunat ENUM('PENDIENTE','ENVIADO','ACEPTADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    codigo_hash VARCHAR(255),
    xml_url VARCHAR(255),
    cdr_url VARCHAR(255),
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio TIMESTAMP NULL,
    CONSTRAINT fk_comprobante_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_comprobante_venta FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE RESTRICT,
    CONSTRAINT uq_comprobante_serie_numero UNIQUE (serie, numero)
);
CREATE INDEX idx_comprobante_empresa ON comprobantes(empresa_id);
CREATE INDEX idx_comprobante_venta ON comprobantes(venta_id);
CREATE INDEX idx_comprobante_fecha ON comprobantes(fecha_emision);
CREATE INDEX idx_comprobante_estado ON comprobantes(estado_sunat);

-- =========================
-- PAGOS
-- =========================
CREATE TABLE IF NOT EXISTS pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    metodo ENUM('EFECTIVO','YAPE','PLIN','TRANSFERENCIA','TARJETA') NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    referencia VARCHAR(100),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_venta FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE
);
CREATE INDEX idx_pago_venta ON pagos(venta_id);
-- Una venta puede tener varios pagos (máx. 3), validado desde el backend,
-- y su suma nunca debe superar el total de la venta.

-- =========================================================================
-- ALTER: agregar 'GUIA' al ENUM de tipo en series/comprobantes. Necesario en
-- este entorno porque esas tablas ya existían (creadas antes con el script
-- de diseño) sin 'GUIA'. Si las tablas se acaban de crear arriba, esto es
-- un no-op porque el ENUM ya las incluye.
-- =========================================================================
ALTER TABLE series
    MODIFY COLUMN tipo ENUM('BOLETA','GUIA','FACTURA','NOTA_CREDITO','NOTA_DEBITO') NOT NULL;

ALTER TABLE comprobantes
    MODIFY COLUMN tipo ENUM('BOLETA','GUIA','FACTURA','NOTA_CREDITO','NOTA_DEBITO') NOT NULL;

-- =========================================================================
-- BACKFILL: empresas ya existentes (creadas antes de este módulo) necesitan
-- su cliente genérico y sus series principales para poder vender.
-- =========================================================================

INSERT INTO clientes (empresa_id, tipo_documento, numero_documento, nombre)
SELECT e.id, 'DNI', '00000000', 'CLIENTE VARIOS'
FROM empresas e
WHERE NOT EXISTS (
    SELECT 1 FROM clientes c WHERE c.empresa_id = e.id AND c.numero_documento = '00000000'
);

INSERT INTO series (empresa_id, tipo, serie, correlativo_actual, es_principal, estado)
SELECT e.id, 'BOLETA', 'B001', 0, 1, 1
FROM empresas e
WHERE NOT EXISTS (
    SELECT 1 FROM series s WHERE s.empresa_id = e.id AND s.tipo = 'BOLETA'
);

INSERT INTO series (empresa_id, tipo, serie, correlativo_actual, es_principal, estado)
SELECT e.id, 'GUIA', 'T001', 0, 1, 1
FROM empresas e
WHERE NOT EXISTS (
    SELECT 1 FROM series s WHERE s.empresa_id = e.id AND s.tipo = 'GUIA'
);
