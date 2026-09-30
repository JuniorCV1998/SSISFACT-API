-- Corrige el bug de sp_crear_o_actualizar_registro_proceso_empresa que insertaba
-- toda empresa nueva confirmada con estado=2 ("suspendida"), cuando en realidad
-- debía nacer estado=1 ("activa"). Ninguna empresa ha sido suspendida nunca por
-- un superadmin todavía, así que es seguro normalizar todas las estado=2 a 1.
UPDATE empresas SET estado = 1 WHERE estado = 2;
