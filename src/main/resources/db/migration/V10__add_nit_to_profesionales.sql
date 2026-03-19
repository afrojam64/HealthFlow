-- Agregar columna nit a la tabla profesionales
ALTER TABLE profesionales ADD COLUMN IF NOT EXISTS nit VARCHAR(20) UNIQUE;