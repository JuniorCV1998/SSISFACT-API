CREATE DATABASE db_ssimple_ssifact;
USE db_ssimple_ssifact;

-- =========================
-- EMPRESAS
-- =========================
CREATE TABLE empresas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    ruc VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(255),
    telefono VARCHAR(50),
    email VARCHAR(100),
    logo_url VARCHAR(255),
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0 → eliminado, 1 → activo, 2 → pendient',
    username_sunat VARCHAR(100) COMMENT 'Usuario SOL/SUNAT, opcional',
    password_sunat VARCHAR(500) COMMENT 'Clave SOL/SUNAT encriptada, opcional',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Se agrega automaticamente en cada registro',
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'se actualiza en cada update'
);

-- =========================
-- SUCURSALES
-- =========================
CREATE TABLE sucursales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(50),
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0 → eliminado, 1 → activo, 2 → pendient',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sucursal_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT uq_sucursal_nombre_empresa UNIQUE (empresa_id, nombre)
);

-- =========================
-- ROLES
-- =========================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0=eliminado,1=activo',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================
-- USUARIOS
-- =========================
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    sucursal_id BIGINT COMMENT 'no para admin', 
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0=eliminado,1=activo',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id) ON DELETE SET NULL ) COMMENT='Usuarios: si se elimina sucursal, usuario queda sin sucursal';

-- =========================
-- USUARIOS ROLES
-- =========================
CREATE TABLE usuario_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
	CONSTRAINT fk_usuario_roles_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE RESTRICT,
	CONSTRAINT uq_usuario_rol UNIQUE (usuario_id, rol_id)
);

INSERT INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Acceso total al sistema'),
('CAJERO', 'Registro de ventas y manejo de caja'),
('SUPERVISOR', 'Supervisión y reportes'),
('ALMACEN', 'Gestión de inventario'),
('AUDITOR', 'Solo lectura del sistema');

-- =========================
-- CATEGORIAS
-- =========================
CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT,
    nombre VARCHAR(150),
    descripcion VARCHAR(255),
    estado TINYINT DEFAULT 1,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    estado TINYINT NOT NULL DEFAULT 1,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_categoria_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT uq_categoria_nombre_empresa UNIQUE (empresa_id, nombre),
    INDEX idx_categoria_empresa (empresa_id)
);

-- =========================
-- AGREGANDO INDICES A TABLAS YA CREADAS, PARA OPTIMIZAR BUSQUEDAS
-- =========================
CREATE INDEX idx_sucursal_empresa ON sucursales(empresa_id);

CREATE INDEX idx_usuario_empresa ON usuarios(empresa_id);
CREATE INDEX idx_usuario_sucursal ON usuarios(sucursal_id);

CREATE INDEX idx_usuario_roles_usuario ON usuario_roles(usuario_id);
CREATE INDEX idx_usuario_roles_rol ON usuario_roles(rol_id);

-- AUN NO
CREATE INDEX idx_venta_caja ON ventas(caja_id);
CREATE INDEX idx_venta_fecha ON ventas(fecha);
CREATE INDEX idx_venta_usuario ON ventas(usuario_id);

CREATE INDEX idx_producto_categoria ON productos(categoria_id);
CREATE INDEX idx_producto_empresa ON productos(empresa_id);

CREATE INDEX idx_caja_usuario ON cajas(usuario_id);
CREATE INDEX idx_caja_sucursal ON cajas(sucursal_id);
CREATE INDEX idx_caja_estado ON cajas(estado);

-- =========================
-- PRODUCTOS
-- =========================
CREATE TABLE productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    costo DECIMAL(10,2),
    codigo_barras VARCHAR(100),
    imagen_url VARCHAR(255),
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0=eliminado,1=activo',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE RESTRICT,
    CONSTRAINT uq_producto_codigo_empresa UNIQUE (empresa_id, codigo_barras),
    CONSTRAINT uq_producto_nombre_empresa UNIQUE (empresa_id, nombre)
);
CREATE INDEX idx_producto_empresa ON productos(empresa_id);
CREATE INDEX idx_producto_categoria ON productos(categoria_id);

-- =========================
-- INVENTARIO
-- =========================
CREATE TABLE inventarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventario_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    CONSTRAINT fk_inventario_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id) ON DELETE CASCADE,
    CONSTRAINT uq_producto_sucursal UNIQUE (producto_id, sucursal_id)
);

CREATE INDEX idx_inventario_producto ON inventarios(producto_id);
CREATE INDEX idx_inventario_sucursal ON inventarios(sucursal_id);

-- =========================
-- MOVIMIENTOS STOCK
-- =========================
CREATE TABLE movimientos_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    tipo ENUM('ENTRADA','SALIDA','AJUSTE','VENTA','COMPRA'),
    cantidad INT NOT NULL,
    motivo VARCHAR(100), -- venta, compra, ajuste
    referencia_id BIGINT, -- id de venta, compra, etc.
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_mov_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id)
);
-- Flujo real de venta:
-- se registra venta
-- se descuenta stock en inventarios
-- se registra en movimientos_stock

-- =========================
-- PROVEEDORES
-- =========================
CREATE TABLE proveedores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    ruc VARCHAR(20),
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(255),
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0=eliminado,1=activo',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_proveedor_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT uq_proveedor_ruc_empresa UNIQUE (empresa_id, ruc)
);

CREATE INDEX idx_proveedor_empresa ON proveedores(empresa_id);

-- =========================
-- COMPRAS
-- =========================
CREATE TABLE compras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    proveedor_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'costo total de la compra',
    estado ENUM('PENDIENTE','CONFIRMADA','ANULADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_compra_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_compra_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id) ON DELETE RESTRICT,
    CONSTRAINT fk_compra_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id) ON DELETE RESTRICT,
    CONSTRAINT fk_compra_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

CREATE INDEX idx_compra_empresa ON compras(empresa_id);
CREATE INDEX idx_compra_sucursal ON compras(sucursal_id);
CREATE INDEX idx_compra_proveedor ON compras(proveedor_id);
CREATE INDEX idx_compra_usuario ON compras(usuario_id);
CREATE INDEX idx_compra_fecha ON compras(fecha);

-- =========================
-- DETALLE COMPRA
-- =========================
CREATE TABLE detalle_compras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compra_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    costo_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_compra FOREIGN KEY (compra_id) REFERENCES compras(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE RESTRICT
);
CREATE INDEX idx_detalle_compra ON detalle_compras(compra_id);
CREATE INDEX idx_detalle_producto ON detalle_compras(producto_id);

-- Flujo real:
-- Insertas en compras
-- Insertas en detalle_compras
-- Aumentas inventarios
-- Insertas en movimientos_stock

-- =========================
-- CLIENTES
-- =========================
CREATE TABLE clientes (
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

-- cliente genérico, agregar desde el backend para todas las empresas nuevas que nacen
INSERT INTO clientes (empresa_id, tipo_documento, numero_documento, nombre) VALUES (
1, 'DNI', '00000000', 'CLIENTE VARIOS');

-- =========================
-- CAJAS
-- =========================

CREATE TABLE cajas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sucursal_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    monto_inicial DECIMAL(12,2) NOT NULL,
    monto_final DECIMAL(12,2),
    fecha_apertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre TIMESTAMP,
    estado ENUM('ABIERTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
    CONSTRAINT fk_caja_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales(id),
    CONSTRAINT fk_caja_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
CREATE INDEX idx_caja_sucursal ON cajas(sucursal_id);
CREATE INDEX idx_caja_usuario ON cajas(usuario_id);
CREATE INDEX idx_caja_estado ON cajas(estado);

-- Validación antes de abrir caja
SELECT id 
FROM cajas
WHERE usuario_id = ? 
AND estado = 'ABIERTA';

-- =========================
-- VENTAS
-- =========================
CREATE TABLE ventas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    sucursal_id BIGINT NOT NULL,
    cliente_id BIGINT,
    usuario_id BIGINT NOT NULL,
    caja_id BIGINT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    impuestos DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL COMMENT 'Total de la venta realizada',
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

-- Flujo real: Crear venta
-- Insertar detalle_venta
-- Descontar inventarios
-- Insertar movimientos_stock (SALIDA)
-- Afectar caja

-- =========================
-- DETALLE VENTA
-- =========================
CREATE TABLE detalle_venta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_venta FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_venta_producto  FOREIGN KEY (producto_id) REFERENCES productos(id)
);
CREATE INDEX idx_detalle_venta ON detalle_venta(venta_id);
CREATE INDEX idx_detalle_producto ON detalle_venta(producto_id);

-- =========================
-- PEDIDOS (TIENDA)
-- =========================
CREATE TABLE pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    envio DECIMAL(12,2) NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,
    estado ENUM('PENDIENTE','PAGADO','ENVIADO','ENTREGADO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT
);
CREATE INDEX idx_pedido_empresa ON pedidos(empresa_id);
CREATE INDEX idx_pedido_cliente ON pedidos(cliente_id);
CREATE INDEX idx_pedido_estado ON pedidos(estado);
CREATE INDEX idx_pedido_fecha ON pedidos(fecha);

-- =========================
-- DETALLE PEDIDO
-- =========================
CREATE TABLE detalle_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_pedido_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE RESTRICT
);
CREATE INDEX idx_detalle_pedido ON detalle_pedido(pedido_id);
CREATE INDEX idx_detalle_pedido_producto ON detalle_pedido(producto_id);

-- =========================
-- DIRECCIONES CLIENTE
-- =========================
CREATE TABLE direcciones_cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    ciudad VARCHAR(100),
    referencia VARCHAR(255),
    principal TINYINT NOT NULL DEFAULT 0 COMMENT '0=no,1=si',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_direccion_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);
CREATE INDEX idx_direccion_cliente ON direcciones_cliente(cliente_id);
-- Evitar múltiples direcciones principales: Desde backend
-- Solo una dirección con principal = 1 por cliente

-- =========================
-- COMPROBANTES (SUNAT)
-- =========================
CREATE TABLE comprobantes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    venta_id BIGINT NOT NULL,
    tipo ENUM('BOLETA','FACTURA','NOTA_CREDITO','NOTA_DEBITO') NOT NULL,
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

-- Cuando generas comprobante AFECTAS a la tabla SERIES:
-- UPDATE series SET correlativo_actual = correlativo_actual + 1

-- =========================
-- MONEDAS
-- =========================
CREATE TABLE monedas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL UNIQUE, -- PEN, USD, EUR
    nombre VARCHAR(50) NOT NULL,
    simbolo VARCHAR(10),
    estado TINYINT NOT NULL DEFAULT 1
);

-- =========================
-- CONFIGURACION
-- =========================
CREATE TABLE configuracion_empresa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    moneda_id BIGINT NOT NULL,
    impuesto DECIMAL(5,2) NOT NULL DEFAULT 18.00,
    stock_minimo INT NOT NULL DEFAULT 5,
    logo_url VARCHAR(255),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_config_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    CONSTRAINT fk_config_moneda FOREIGN KEY (moneda_id) REFERENCES monedas(id),
    CONSTRAINT uq_config_empresa UNIQUE (empresa_id)
);

-- =========================
-- SERIES
-- =========================
CREATE TABLE series (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    tipo ENUM('BOLETA','FACTURA','NOTA_CREDITO','NOTA_DEBITO') NOT NULL,
    serie VARCHAR(10) NOT NULL,
    correlativo_actual INT NOT NULL DEFAULT 0,
    es_principal TINYINT NOT NULL DEFAULT 1 COMMENT '1=principal',
    estado TINYINT NOT NULL DEFAULT 1 COMMENT '0=inactivo,1=activo',
    UNIQUE (empresa_id, tipo, serie),
    CONSTRAINT fk_series_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);
CREATE INDEX idx_series_empresa_tipo ON series(empresa_id, tipo);
CREATE INDEX idx_series_principal ON series(empresa_id, tipo, es_principal);

-- Luego de crear la empresa, agregar las series:
-- INSERT INTO series (empresa_id, tipo, serie, correlativo_actual)
-- VALUES
-- (1, 'FACTURA', 'F001', 0),
-- (1, 'BOLETA', 'B001', 0),
-- (1, 'NOTA_CREDITO', 'FC01', 0),
-- (1, 'NOTA_DEBITO', 'FD01', 0);

-- =========================
-- PAGOS
-- =========================
CREATE TABLE pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    metodo ENUM('EFECTIVO','YAPE','PLIN','TRANSFERENCIA','TARJETA') NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    referencia VARCHAR(100),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_venta FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE
);
-- Una venta puede tener varios pagos, pero backend deberia de validar que todos los pagos no superen el total de la venta
-- y poder agregar a lo mucho 3 diferentes pagos

-- =========================
-- SUNAT_NOTIFICACIONES
-- =========================
CREATE TABLE sunat_notificaciones (
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
CREATE TABLE sunat_mensajes (
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

-- EMPRESA
INSERT INTO empresas (nombre, ruc) VALUES ('Mi Empresa Demo', '12345678901');
-- SUCURSAL
INSERT INTO sucursales (empresa_id, nombre) VALUES (1, 'Sucursal Principal');
-- USER ADMIN
INSERT INTO usuarios (empresa_id, sucursal_id, nombre, email, password, rol_id)
VALUES (1, 1, 'Admin', 'admin@demo.com', '123456', 1);


-- =========================
-- REGISTER PROCESS
-- =========================

create table register_process (
	idRegisterProcess int NOT NULL AUTO_INCREMENT,
    ruc varchar(11) NOT NULL,
    razonSocial varchar(120) NOT NULL,
    telefono VARCHAR(15) NULL,
    correo varchar(70) NOT NULL,
    documento varchar(12) NULL,
    nombre varchar(100) NULL,
    contrasena varchar(120) NULL,
    estado char(1) NULL,
    
	date_create datetime NULL,
    date_update datetime NULL,
    PRIMARY KEY (idRegisterProcess)
);
