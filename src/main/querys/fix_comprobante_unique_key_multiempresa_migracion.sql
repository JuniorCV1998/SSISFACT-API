-- La unicidad de comprobantes era (serie, numero) SIN empresa_id, por lo que
-- dos empresas distintas cuya primera sucursal usa el mismo código de serie
-- (ej. NP01, caso normal al crearse) colisionan apenas ambas emiten el mismo
-- número. Se corrige para que la unicidad sea por empresa.
ALTER TABLE comprobantes DROP INDEX uq_comprobante_serie_numero;
ALTER TABLE comprobantes
    ADD UNIQUE KEY uq_comprobante_empresa_serie_numero (empresa_id, serie, numero);

-- Sincroniza el correlativo de cada serie con el número máximo ya emitido
-- bajo ese código dentro de la misma empresa. Cubre el caso real detectado:
-- la migración series_por_sucursal_migracion.sql reasignó códigos de serie
-- (ej. NP01 pasó de la sucursal 4 a la sucursal 1) sin heredar los números
-- que ya se habían emitido con ese código, dejando el contador atrasado y
-- listo para repetir un número ya usado.
UPDATE series s
JOIN (
    SELECT empresa_id, serie, tipo, MAX(numero) AS max_numero
    FROM comprobantes
    GROUP BY empresa_id, serie, tipo
) m ON m.empresa_id = s.empresa_id AND m.serie = s.serie AND m.tipo = s.tipo
SET s.correlativo_actual = m.max_numero
WHERE s.correlativo_actual < m.max_numero;
