-- Agregar columna slug a la tabla profesionales
ALTER TABLE profesionales ADD COLUMN slug VARCHAR(100) UNIQUE;

-- Generar slugs únicos basados en nombre_completo (versión inicial)
UPDATE profesionales SET slug = LOWER(REGEXP_REPLACE(nombre_completo, '[^a-zA-Z0-9]+', '-', 'g'));

-- En caso de duplicados (poco probable con datos de prueba), se resuelve con un sufijo numérico
-- pero no es necesario para este conjunto de datos.