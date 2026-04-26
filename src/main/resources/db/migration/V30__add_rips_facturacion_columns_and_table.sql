-- Agregar columnas a profesionales
ALTER TABLE profesionales ADD COLUMN IF NOT EXISTS tipo_facturacion VARCHAR(10) NOT NULL DEFAULT 'INFORMAL';
ALTER TABLE profesionales ADD COLUMN IF NOT EXISTS cod_prestador VARCHAR(12);

-- Agregar columna factura_numero a citas
ALTER TABLE citas ADD COLUMN IF NOT EXISTS factura_numero VARCHAR(50);

-- Agregar columnas a pacientes
ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS tipo_usuario VARCHAR(2) DEFAULT '01';
ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS cod_pais_residencia VARCHAR(3) DEFAULT '170';
ALTER TABLE pacientes ADD COLUMN IF NOT EXISTS cod_zona_residencia VARCHAR(2) DEFAULT '01';

-- Crear tabla rips_generations
CREATE TABLE IF NOT EXISTS rips_generations (
                                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    professional_id UUID NOT NULL,
    fecha_desde DATE NOT NULL,
    fecha_hasta DATE NOT NULL,
    fecha_generacion TIMESTAMP NOT NULL,
    tipo_generacion VARCHAR(10) NOT NULL,
    num_factura VARCHAR(50),
    tipo_nota VARCHAR(10),
    num_nota VARCHAR(50),
    archivo_path VARCHAR(500),
    automatica BOOLEAN DEFAULT FALSE,
    cuv VARCHAR(50),
    total_registros INTEGER,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_rips_generations_professional ON rips_generations(professional_id);