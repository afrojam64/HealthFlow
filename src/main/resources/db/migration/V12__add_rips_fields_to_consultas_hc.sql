-- =====================================================
-- Migración V12: Agregar campos RIPS a la tabla consultas_hc
-- =====================================================

-- Agregar modalidad de consulta (ej: 01=Presencial, 02=Telemedicina)
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS modalidad_consulta VARCHAR(2);

-- Agregar grupo de servicios (según tabla maestra)
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS grupo_servicios VARCHAR(2);

-- Agregar vía de ingreso del usuario
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS via_ingreso VARCHAR(2);

-- Agregar tipo de diagnóstico (1=Impresión diagnóstica, 2=Confirmado nuevo, 3=Confirmado repetido)
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS tipo_diagnostico VARCHAR(1);

-- Agregar código CUPS de la consulta
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS codigo_cups VARCHAR(10);

-- Agregar valores económicos (si no existen)
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS valor_servicio DECIMAL(19,2);
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS cuota_moderadora DECIMAL(19,2);
ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS copago DECIMAL(19,2);