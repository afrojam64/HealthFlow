CREATE TABLE receta_medicamentos (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     receta_id UUID NOT NULL REFERENCES recetas(id) ON DELETE CASCADE,
                                     medicamento_id UUID NOT NULL REFERENCES medicamentos(id),
                                     dosis VARCHAR(100) NOT NULL,
                                     frecuencia VARCHAR(100) NOT NULL,
                                     cantidad INTEGER NOT NULL DEFAULT 1,
                                     duracion VARCHAR(100),
                                     instrucciones TEXT,
                                     version INTEGER NOT NULL DEFAULT 0,
                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                                     updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_receta_medicamentos_receta ON receta_medicamentos(receta_id);