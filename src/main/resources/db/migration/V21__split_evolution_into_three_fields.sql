-- Agregar tres nuevas columnas a la tabla consultas_hc
ALTER TABLE consultas_hc ADD COLUMN enfermedad_actual TEXT;
ALTER TABLE consultas_hc ADD COLUMN examen_fisico TEXT;
ALTER TABLE consultas_hc ADD COLUMN concepto TEXT;

-- Nota: La columna 'evolucion' se conserva para compatibilidad con registros antiguos.
-- Los nuevos registros usarán las tres nuevas columnas.