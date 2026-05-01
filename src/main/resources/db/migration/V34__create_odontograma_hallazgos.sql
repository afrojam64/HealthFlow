-- ============================================================
-- Migración V34: Crear tabla de hallazgos odontológicos
-- ============================================================

CREATE TABLE IF NOT EXISTS odontograma_hallazgos (
                                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cita_id UUID NOT NULL,
    diente SMALLINT NOT NULL,
    cara VARCHAR(20),
    tipo_hallazgo VARCHAR(50) NOT NULL,
    cups_id BIGINT, -- NULL si el hallazgo no es un procedimiento (ej. caries sin tratar)
    valor_json JSONB, -- Datos específicos (ej. periodontograma, endodoncia)
    es_inicial BOOLEAN NOT NULL DEFAULT FALSE, -- TRUE para el primer odontograma del paciente
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    FOREIGN KEY (cita_id) REFERENCES citas(id) ON DELETE CASCADE,
    FOREIGN KEY (cups_id) REFERENCES cups_catalogos(id) ON DELETE SET NULL
    );

COMMENT ON TABLE odontograma_hallazgos IS 'Registra hallazgos dentales por cita (caries, obturaciones, periodontograma, etc.)';
COMMENT ON COLUMN odontograma_hallazgos.diente IS 'Número FDI (1-32 para permanentes, 51-85 para temporales)';
COMMENT ON COLUMN odontograma_hallazgos.cara IS 'Cara del diente afectada: VESTIBULAR, LINGUAL, MESIAL, DISTAL, OCLUSAL, INCISAL o TODAS';
COMMENT ON COLUMN odontograma_hallazgos.tipo_hallazgo IS 'CARIES, OBTURACION, EXTRACCION, PROSTODONCIA, ENDODONCIA, PERIODONCIA, SELLANTE, etc.';
COMMENT ON COLUMN odontograma_hallazgos.valor_json IS 'Datos variables según especialidad (ej. profundidad de bolsa para periodoncia)';
COMMENT ON COLUMN odontograma_hallazgos.es_inicial IS 'Indica si pertenece al odontograma inicial del paciente (primer diagnóstico)';

-- Índices para consultas rápidas
CREATE INDEX idx_odontograma_cita ON odontograma_hallazgos(cita_id);
CREATE INDEX idx_odontograma_paciente ON odontograma_hallazgos(cita_id) INCLUDE (diente); -- podríamos mejorar, suficiente por ahora