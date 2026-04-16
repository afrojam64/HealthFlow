CREATE TABLE recetas (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         numero VARCHAR(50) NOT NULL UNIQUE,
                         paciente_id UUID NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
                         profesional_id UUID NOT NULL REFERENCES profesionales(id) ON DELETE CASCADE,
                         cita_id UUID REFERENCES citas(id) ON DELETE SET NULL,
                         fecha_emision TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                         fecha_expiracion TIMESTAMP WITH TIME ZONE NOT NULL,
                         estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
                         token UUID UNIQUE,
                         observaciones TEXT,
                         version INTEGER NOT NULL DEFAULT 0,
                         created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recetas_paciente ON recetas(paciente_id);
CREATE INDEX idx_recetas_profesional ON recetas(profesional_id);
CREATE INDEX idx_recetas_cita ON recetas(cita_id);
CREATE INDEX idx_recetas_estado ON recetas(estado);