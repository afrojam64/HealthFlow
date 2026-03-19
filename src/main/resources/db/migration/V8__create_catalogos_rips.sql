-- =====================================================
-- Migración V8: Crear catálogos RIPS y modificar consultas_hc
-- =====================================================

-- 1. Crear tabla de finalidad de consulta
CREATE TABLE IF NOT EXISTS catalogo_finalidad_consulta (
                                                           id BIGSERIAL PRIMARY KEY,
                                                           codigo VARCHAR(2) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
    );

-- 2. Crear tabla de causa externa
CREATE TABLE IF NOT EXISTS catalogo_causa_externa (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      codigo VARCHAR(2) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
    );

-- 3. Agregar columnas a la tabla consultas_hc (MedicalRecord)
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS finalidad_consulta_id BIGINT;
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS causa_externa_id BIGINT;
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS modalidad_consulta VARCHAR(2);
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS grupo_servicios VARCHAR(2);
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS via_ingreso VARCHAR(2);
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS tipo_diagnostico VARCHAR(1);
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS valor_servicio DECIMAL(19,2);
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS cuota_moderadora DECIMAL(19,2);
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS copago DECIMAL(19,2);

-- 4. Añadir claves foráneas
ALTER TABLE consultas_hc ADD CONSTRAINT fk_consultas_hc_finalidad
    FOREIGN KEY (finalidad_consulta_id) REFERENCES catalogo_finalidad_consulta(id);

ALTER TABLE consultas_hc ADD CONSTRAINT fk_consultas_hc_causa
    FOREIGN KEY (causa_externa_id) REFERENCES catalogo_causa_externa(id);