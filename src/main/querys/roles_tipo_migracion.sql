-- =========================================================================
-- CLASIFICACIÓN DE ROLES POR TIPO — quién puede asignar cada rol.
--
-- NOTA: en este entorno la columna ya fue agregada manualmente antes de
-- versionarla aquí (por eso este script no se corrió como parte de esta
-- conversación) — se documenta para que otros entornos puedan aplicarla.
-- No idempotente (MySQL 8.0.40 no soporta ADD COLUMN IF NOT EXISTS): si se
-- corre contra una BD que ya la tiene, falla con "Duplicate column name",
-- es esperado, no correrlo dos veces.
--
-- tipo:
--   ADMIN  -> rol de alto privilegio dentro de una empresa (ADMIN, SUNAT).
--             Solo el SUPERADMIN de la plataforma puede asignarlo
--             (POST /admin/empresas/{id}/usuarios/{usuarioId}/rol).
--   USER   -> rol operativo de trabajador (CAJERO, SUPERVISOR, ALMACEN,
--             AUDITOR). El admin de CADA empresa puede asignarlo a sus
--             propios trabajadores (POST /usuario/{id}/rol).
--   SYSTEM -> reservado a la plataforma (SUPERADMIN). No se asigna desde
--             ningún endpoint de administración de empresas.
-- =========================================================================

ALTER TABLE roles
    ADD COLUMN tipo VARCHAR(10) NULL AFTER estado;

UPDATE roles SET tipo = 'ADMIN'  WHERE nombre IN ('ADMIN', 'SUNAT');
UPDATE roles SET tipo = 'USER'   WHERE nombre IN ('CAJERO', 'SUPERVISOR', 'ALMACEN', 'AUDITOR');
UPDATE roles SET tipo = 'SYSTEM' WHERE nombre = 'SUPERADMIN';
