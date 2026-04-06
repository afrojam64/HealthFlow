CREATE TABLE medicamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    nombre_generico VARCHAR(200) NOT NULL,
    presentacion VARCHAR(100),
    concentracion VARCHAR(50),
    forma_farmaceutica VARCHAR(100),
    via_administracion VARCHAR(50)
);

CREATE INDEX idx_medicamentos_nombre ON medicamentos USING GIN (to_tsvector('spanish', nombre_generico));

ALTER TABLE consultas_hc ADD COLUMN IF NOT EXISTS prescripcion_json JSONB;