-- HealthFlow MVP schema (HU01–HU03)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS usuarios (
  id UUID PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  rol VARCHAR(20) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS profesionales (
  id UUID PRIMARY KEY,
  usuario_id UUID NULL REFERENCES usuarios(id),
  nombre_completo VARCHAR(150) NOT NULL,
  registro_medico VARCHAR(50) UNIQUE NOT NULL,
  especialidad VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS pacientes (
  id UUID PRIMARY KEY,
  tipo_doc VARCHAR(2) NOT NULL,
  num_doc VARCHAR(20) UNIQUE NOT NULL,
  nombre1 VARCHAR(50) NOT NULL,
  nombre2 VARCHAR(50) NULL,
  apellido1 VARCHAR(50) NOT NULL,
  apellido2 VARCHAR(50) NULL,
  fecha_nac DATE NOT NULL,
  sexo CHAR(1) NOT NULL,
  cod_municipio CHAR(5) NOT NULL,
  email VARCHAR(100) NOT NULL,
  celular VARCHAR(15) NOT NULL
);

CREATE TABLE IF NOT EXISTS disponibilidad_base (
  id UUID PRIMARY KEY,
  profesional_id UUID NOT NULL REFERENCES profesionales(id),
  dia_semana INT NOT NULL CHECK (dia_semana BETWEEN 0 AND 6),
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL
);

CREATE TABLE IF NOT EXISTS excepciones_agenda (
  id UUID PRIMARY KEY,
  profesional_id UUID NOT NULL REFERENCES profesionales(id),
  fecha_especifica DATE NOT NULL,
  hora_inicio TIME NULL,
  hora_fin TIME NULL,
  tipo VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS citas (
  id UUID PRIMARY KEY,
  paciente_id UUID NOT NULL REFERENCES pacientes(id),
  profesional_id UUID NOT NULL REFERENCES profesionales(id),
  fecha_hora TIMESTAMPTZ NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PEND',
  token_acceso UUID UNIQUE
);

-- RNF1: evitar doble agendamiento
CREATE UNIQUE INDEX IF NOT EXISTS uk_citas_prof_fecha ON citas(profesional_id, fecha_hora);
