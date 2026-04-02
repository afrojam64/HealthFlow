CREATE TABLE paciente_token (
                                id BIGSERIAL PRIMARY KEY,
                                paciente_id BIGINT NOT NULL,
                                token VARCHAR(36) NOT NULL UNIQUE,
                                expira_en TIMESTAMP NOT NULL,
                                activo BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                CONSTRAINT fk_paciente_token_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE
);

CREATE INDEX idx_paciente_token_token ON paciente_token(token);
CREATE INDEX idx_paciente_token_paciente_activo ON paciente_token(paciente_id, activo);