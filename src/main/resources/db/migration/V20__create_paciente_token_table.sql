CREATE TABLE paciente_token (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    paciente_id UUID NOT NULL,
    token VARCHAR(36) NOT NULL UNIQUE,
    expira_en TIMESTAMP WITH TIME ZONE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_paciente_token PRIMARY KEY (id),
    CONSTRAINT fk_paciente_token_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
);

CREATE INDEX idx_paciente_token_token ON paciente_token(token);
CREATE INDEX idx_paciente_token_paciente_activo ON paciente_token(paciente_id, activo);