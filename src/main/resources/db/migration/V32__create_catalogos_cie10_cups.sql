-- ============================================================
-- Migración V32: Crear/actualizar catálogos para RIPS
-- ============================================================

-- 1. Agregar columna 'activo' a diagnosticos_cie10 (si no existe)
ALTER TABLE diagnosticos_cie10 ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE;

-- 2. Crear tabla cups_catalogos
CREATE TABLE IF NOT EXISTS cups_catalogos (
                                              id BIGSERIAL PRIMARY KEY,
                                              codigo VARCHAR(10) NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );

-- 3. Crear índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_cups_codigo ON cups_catalogos(codigo);
CREATE INDEX IF NOT EXISTS idx_diagnosticos_cie10_codigo ON diagnosticos_cie10(codigo);