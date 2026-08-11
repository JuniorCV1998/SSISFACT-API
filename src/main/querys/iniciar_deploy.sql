INSERT INTO monedas (codigo, nombre, simbolo, estado) VALUES
('PEN', 'Sol Peruano', 'S/', 1),
('USD', 'Dólar Estadounidense', '$', 1),
('EUR', 'Euro', '€', 1);

INSERT INTO configuracion_sistema (
    tipo_configuracion,
    moneda_id,
    impuesto_default,
    max_sucursal_default,
    max_usuarios_default,
    estado
) VALUES (
    'PERU',
    1,              -- ID de PEN en tabla monedas
    18.00,
    5,
    5,
    '1'
);

INSERT INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Acceso total al sistema'),
('CAJERO', 'Registro de ventas y manejo de caja'),
('SUPERVISOR', 'Supervisión y reportes'),
('ALMACEN', 'Gestión de inventario'),
('AUDITOR', 'Solo lectura del sistema');