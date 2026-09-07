-- Las series de NOTA_PEDIDO pasan de ser una por empresa a una por sucursal,
-- igual que hace SUNAT con los comprobantes electrónicos reales (cada
-- establecimiento anexo tiene su propia serie). BOLETA/GUIA quedan igual por
-- ahora (empresa-wide, sucursal_id NULL) porque todavía no están en uso.

ALTER TABLE series ADD COLUMN sucursal_id BIGINT NULL AFTER empresa_id;

ALTER TABLE series
    ADD CONSTRAINT fk_series_sucursal FOREIGN KEY (sucursal_id) REFERENCES sucursales (id) ON DELETE CASCADE;

ALTER TABLE series DROP INDEX empresa_id;
ALTER TABLE series ADD UNIQUE KEY uq_series_empresa_sucursal_tipo_serie (empresa_id, sucursal_id, tipo, serie);

-- Quita las series NOTA_PEDIDO empresa-wide creadas antes de este cambio.
DELETE FROM series WHERE tipo = 'NOTA_PEDIDO' AND sucursal_id IS NULL;

-- Una serie NOTA_PEDIDO por cada sucursal ya existente: NP01, NP02... en el
-- orden de creación de sus sucursales dentro de cada empresa.
INSERT INTO series (empresa_id, sucursal_id, tipo, serie, correlativo_actual, es_principal, estado)
SELECT
    s.empresa_id,
    s.id,
    'NOTA_PEDIDO',
    CONCAT('NP', LPAD(ROW_NUMBER() OVER (PARTITION BY s.empresa_id ORDER BY s.id), 2, '0')),
    0, 1, 1
FROM sucursales s;
