CREATE TABLE especialidades (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

INSERT INTO especialidades (nombre) VALUES
('Cardiología'),
('Dermatología'),
('Endocrinología'),
('Gastroenterología'),
('Ginecología'),
('Medicina Interna'),
('Neumología'),
('Neurología'),
('Oftalmología'),
('Ortopedia'),
('Otorrinolaringología'),
('Pediatría'),
('Psiquiatría'),
('Urología');