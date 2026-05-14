-- V40__create_catalogo_examenes_tables.sql

-- Tabla de catálogo de exámenes individuales
CREATE TABLE catalogo_examenes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_cups VARCHAR(20) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL,
    modalidad VARCHAR(50),
    region_anatomica VARCHAR(100),
    requiere_contraste BOOLEAN DEFAULT FALSE,
    tipo_muestra VARCHAR(50),
    version_cups VARCHAR(10),
    fecha_vigencia DATE,
    activo BOOLEAN DEFAULT TRUE,
    version INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Tabla de perfiles clínicos
CREATE TABLE perfiles_clinicos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    version INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Tabla de relación entre perfiles y exámenes
CREATE TABLE perfil_examen (
    perfil_id UUID NOT NULL REFERENCES perfiles_clinicos(id) ON DELETE CASCADE,
    examen_id UUID NOT NULL REFERENCES catalogo_examenes(id) ON DELETE CASCADE,
    orden INT,
    PRIMARY KEY (perfil_id, examen_id)
);

-- Tabla cabecera de orden de examen
CREATE TABLE orden_examen (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cita_id UUID NOT NULL REFERENCES citas(id) ON DELETE CASCADE,
    paciente_id UUID NOT NULL REFERENCES pacientes(id) ON DELETE CASCADE,
    profesional_id UUID NOT NULL REFERENCES profesionales(id) ON DELETE CASCADE,
    fecha_solicitud TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones_generales TEXT,
    documento_id UUID,
    version INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Tabla detalle de orden de examen
CREATE TABLE orden_examen_detalle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES orden_examen(id) ON DELETE CASCADE,
    examen_id UUID REFERENCES catalogo_examenes(id) ON DELETE SET NULL,
    perfil_id UUID REFERENCES perfiles_clinicos(id) ON DELETE SET NULL,
    cups_codigo VARCHAR(20) NOT NULL,
    nombre_examen VARCHAR(255) NOT NULL,
    instrucciones_especificas TEXT,
    version INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Índices
CREATE INDEX idx_catalogo_examenes_cups ON catalogo_examenes(codigo_cups);
CREATE INDEX idx_catalogo_examenes_nombre ON catalogo_examenes(nombre);
CREATE INDEX idx_catalogo_examenes_categoria ON catalogo_examenes(categoria);
CREATE INDEX idx_perfiles_clinicos_nombre ON perfiles_clinicos(nombre);
CREATE INDEX idx_orden_examen_cita ON orden_examen(cita_id);
CREATE INDEX idx_orden_examen_paciente ON orden_examen(paciente_id);
CREATE INDEX idx_orden_examen_detalle_orden ON orden_examen_detalle(orden_id);