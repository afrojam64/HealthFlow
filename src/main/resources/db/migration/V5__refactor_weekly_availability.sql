-- V5: Refactorización de la entidad WeeklyAvailability para alinearla con BaseEntity

-- 1. Añadir columnas de auditoría y versión a la tabla disponibilidad_semanal
ALTER TABLE disponibilidad_semanal ADD COLUMN version INT;
ALTER TABLE disponibilidad_semanal ADD COLUMN created_at TIMESTAMPTZ;
ALTER TABLE disponibilidad_semanal ADD COLUMN updated_at TIMESTAMPTZ;

-- 2. Establecer valores por defecto para las nuevas columnas en registros existentes
-- (usamos NOW() para created_at y version 0 para empezar)
UPDATE disponibilidad_semanal SET version = 0, created_at = NOW() WHERE created_at IS NULL;

-- 3. Hacer las columnas NOT NULL después de poblarlas
ALTER TABLE disponibilidad_semanal ALTER COLUMN version SET NOT NULL;
ALTER TABLE disponibilidad_semanal ALTER COLUMN created_at SET NOT NULL;

-- 4. Añadir la restricción de clave foránea (FOREIGN KEY) para profesional_id
-- Esto asegura la integridad referencial con la tabla de profesionales.
ALTER TABLE disponibilidad_semanal
    ADD CONSTRAINT fk_disponibilidad_semanal_profesionales
    FOREIGN KEY (profesional_id) REFERENCES profesionales(id);
