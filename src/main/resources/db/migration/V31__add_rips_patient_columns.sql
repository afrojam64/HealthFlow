-- Agregar columna cod_prestador a profesionales
ALTER TABLE profesionales ADD COLUMN IF NOT EXISTS cod_prestador VARCHAR(12);

-- Agregar columnas faltantes a pacientes
ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS tipo_usuario VARCHAR(2) DEFAULT '01';
ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS cod_pais_residencia VARCHAR(3) DEFAULT '170';
ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS cod_zona_residencia VARCHAR(2) DEFAULT '01';