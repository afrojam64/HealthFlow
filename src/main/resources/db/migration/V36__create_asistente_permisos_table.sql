CREATE TABLE IF NOT EXISTS asistente_permisos (
                                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    medico_id UUID NOT NULL,
    asistente_id UUID NOT NULL,
    permiso VARCHAR(50) NOT NULL,
    concedido BOOLEAN DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_permisos_medico FOREIGN KEY (medico_id) REFERENCES profesionales(id) ON DELETE CASCADE,
    CONSTRAINT fk_permisos_asistente FOREIGN KEY (asistente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT uk_permisos_unique UNIQUE (medico_id, asistente_id, permiso)
    );