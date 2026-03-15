-- Eliminar la tabla existente para limpiar cualquier estado inconsistente
DROP TABLE IF EXISTS consultas_hc;

-- Volver a crear la tabla con la estructura correcta y definitiva
CREATE TABLE consultas_hc (
    id UUID PRIMARY KEY,
    version INT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    cita_id UUID NOT NULL UNIQUE,
    motivo TEXT NOT NULL,
    evolucion TEXT NOT NULL,
    prescripcion TEXT,
    dx_principal VARCHAR(10),
    bloqueado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_consultas_hc_citas FOREIGN KEY (cita_id) REFERENCES citas(id)
);
