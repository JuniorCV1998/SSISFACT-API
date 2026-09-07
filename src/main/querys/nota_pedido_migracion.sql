-- Agrega NOTA_PEDIDO como tipo de comprobante: documento interno, numeración
-- propia (serie NP01), NO se declara ante SUNAT. Se usa mientras no exista
-- facturación electrónica; el día que se active SUNAT, BOLETA/FACTURA quedan
-- disponibles como los tipos reales declarados (ya soportados en el enum).

ALTER TABLE comprobantes
    MODIFY COLUMN tipo ENUM('BOLETA','GUIA','FACTURA','NOTA_CREDITO','NOTA_DEBITO','NOTA_PEDIDO') NOT NULL;

ALTER TABLE series
    MODIFY COLUMN tipo ENUM('BOLETA','GUIA','FACTURA','NOTA_CREDITO','NOTA_DEBITO','NOTA_PEDIDO') NOT NULL;

-- Backfill: una serie NOTA_PEDIDO para cada empresa que ya tiene BOLETA/GUIA
-- (las empresas nuevas la reciben automáticamente desde
-- sp_crear_o_actualizar_registro_proceso_empresa).
INSERT INTO series (empresa_id, tipo, serie, correlativo_actual, es_principal, estado)
SELECT DISTINCT empresa_id, 'NOTA_PEDIDO', 'NP01', 0, 1, 1
FROM series s
WHERE NOT EXISTS (
    SELECT 1 FROM series s2 WHERE s2.empresa_id = s.empresa_id AND s2.tipo = 'NOTA_PEDIDO'
);
