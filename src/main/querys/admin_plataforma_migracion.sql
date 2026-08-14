-- =========================================================================
-- PANEL SUPERADMIN — empresa fantasma + rol SUPERADMIN + campos de plan.
-- Idempotente: se puede correr varias veces sin duplicar datos.
-- =========================================================================

-- =========================
-- CAMPOS DE MEMBRESÍA en configuracion_empresa
-- NOTA: MySQL no soporta "ADD COLUMN IF NOT EXISTS" (eso es sintaxis MariaDB).
-- Este bloque NO es re-ejecutable: si ya corriste esta migración una vez,
-- sáltalo la próxima vez (o comenta estas dos líneas).
-- =========================
ALTER TABLE configuracion_empresa
    ADD COLUMN plan VARCHAR(20) NOT NULL DEFAULT 'FREE',
    ADD COLUMN fecha_vencimiento DATE NULL;

-- =========================
-- ROL SUPERADMIN
-- =========================
INSERT INTO roles (nombre, descripcion)
SELECT 'SUPERADMIN', 'Dueño de la plataforma: administra todas las empresas'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'SUPERADMIN');

-- =========================
-- EMPRESA FANTASMA (a la que pertenece el superadmin)
-- =========================
INSERT INTO empresas (nombre, ruc, email, estado, fecha_creacion)
SELECT 'SSISFACT - Plataforma', '00000000000', 'plataforma@ssisfact.local', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM empresas WHERE ruc = '00000000000');

INSERT INTO configuracion_empresa (empresa_id, moneda_id, impuesto, max_sucursal, max_usuarios, plan, fecha_actualizacion)
SELECT e.id, 1, 0.00, 0, 1, 'PLATFORM', NOW()
FROM empresas e
WHERE e.ruc = '00000000000'
AND NOT EXISTS (SELECT 1 FROM configuracion_empresa ce WHERE ce.empresa_id = e.id);

-- =========================
-- USUARIO SUPERADMIN
-- =========================
INSERT INTO usuarios (empresa_id, sucursal_id, nombre, email, documento, contrasena, estado, fecha_creacion)
SELECT e.id, NULL, 'Super Admin', 'admin@ssimple.com', 'SUPERADMIN01',
       '$2a$10$hRPkGSU7V5cufYSob/V.V.Dqy/tEgdSr5FuaxFLfKjKqkL3iZmz4a', 1, NOW()
FROM empresas e
WHERE e.ruc = '00000000000'
AND NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@ssimple.com');

INSERT INTO usuario_roles (usuario_id, rol_id, fecha_creacion)
SELECT u.id, r.id, NOW()
FROM usuarios u
JOIN roles r ON r.nombre = 'SUPERADMIN'
WHERE u.email = 'admin@ssimple.com'
AND NOT EXISTS (
    SELECT 1 FROM usuario_roles ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id
);
