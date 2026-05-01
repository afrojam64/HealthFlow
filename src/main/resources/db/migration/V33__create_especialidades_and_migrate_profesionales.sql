-- ============================================================
-- Migración V33: Crear catálogo de especialidades y relación muchos a muchos
-- ============================================================

-- 1. Crear tabla de especialidades (solo si no existe)
CREATE TABLE IF NOT EXISTS especialidades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. Agregar columna categoria si no existe
ALTER TABLE especialidades ADD COLUMN IF NOT EXISTS categoria VARCHAR(20) NOT NULL DEFAULT 'MEDICA';

-- 3. Crear tabla intermedia (solo si no existe)
CREATE TABLE IF NOT EXISTS profesional_especialidad (
    professional_id UUID NOT NULL,
    especialidad_id BIGINT NOT NULL,
    PRIMARY KEY (professional_id, especialidad_id),
    FOREIGN KEY (professional_id) REFERENCES profesionales(id) ON DELETE CASCADE,
    FOREIGN KEY (especialidad_id) REFERENCES especialidades(id) ON DELETE CASCADE
);

-- 4. Insertar especialidades únicas desde profesionales (solo si no existen)
INSERT INTO especialidades (nombre, categoria)
SELECT DISTINCT p.especialidad,
       CASE
           WHEN p.especialidad ILIKE '%odontología%' OR p.especialidad ILIKE '%endodoncia%' OR p.especialidad ILIKE '%periodoncia%' OR p.especialidad ILIKE '%ortodoncia%' OR p.especialidad ILIKE '%cirugía oral%' OR p.especialidad ILIKE '%odontopediatría%'
           THEN 'ODONTOLOGICA'
           ELSE 'MEDICA'
       END
FROM profesionales p
WHERE p.especialidad IS NOT NULL AND p.especialidad != ''
ON CONFLICT (nombre) DO NOTHING;

-- 5. Asegurar unicidad del nombre (si aún no existe)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_especialidades_nombre') THEN
        ALTER TABLE especialidades ADD CONSTRAINT uk_especialidades_nombre UNIQUE (nombre);
    END IF;
END $$;

-- 6. Asociar cada profesional con su especialidad actual (solo si no existe la relación)
INSERT INTO profesional_especialidad (professional_id, especialidad_id)
SELECT p.id, e.id
FROM profesionales p
JOIN especialidades e ON p.especialidad = e.nombre
WHERE p.especialidad IS NOT NULL AND p.especialidad != ''
ON CONFLICT (professional_id, especialidad_id) DO NOTHING;