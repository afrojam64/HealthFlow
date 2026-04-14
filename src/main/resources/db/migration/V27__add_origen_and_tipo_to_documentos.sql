-- Agregar columna origen (MEDICO o PACIENTE)
ALTER TABLE documentos ADD COLUMN origen VARCHAR(20) NOT NULL DEFAULT 'MEDICO';

-- Agregar columna tipo_documento (FORMULA, REMISION, LABORATORIO, RADIOGRAFIA, OTRO)
ALTER TABLE documentos ADD COLUMN tipo_documento VARCHAR(50);

-- Índices para búsqueda eficiente
CREATE INDEX idx_documentos_origen ON documentos(origen);
CREATE INDEX idx_documentos_tipo ON documentos(tipo_documento);
CREATE INDEX idx_documentos_fechas ON documentos(created_at);