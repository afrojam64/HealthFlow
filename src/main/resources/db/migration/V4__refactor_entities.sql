-- V4: Refactorización de Entidades a BaseEntity y Relaciones JPA

-- 1. Añadir columnas de auditoría y versión a todas las tablas principales

ALTER TABLE usuarios ADD COLUMN version INT;
ALTER TABLE usuarios ADD COLUMN created_at TIMESTAMPTZ;
ALTER TABLE usuarios ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE profesionales ADD COLUMN version INT;
ALTER TABLE profesionales ADD COLUMN created_at TIMESTAMPTZ;
ALTER TABLE profesionales ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE pacientes ADD COLUMN version INT;
-- Solución temporal: Se cambia ALTER por ADD para que la migración se ejecute en este equipo
ALTER TABLE pacientes ADD COLUMN created_at TIMESTAMPTZ;
ALTER TABLE pacientes ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE citas ADD COLUMN version INT;
ALTER TABLE citas ADD COLUMN created_at TIMESTAMPTZ;
ALTER TABLE citas ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE disponibilidad_base ADD COLUMN version INT;
ALTER TABLE disponibilidad_base ADD COLUMN created_at TIMESTAMPTZ;
ALTER TABLE disponibilidad_base ADD COLUMN updated_at TIMESTAMPTZ;

ALTER TABLE excepciones_agenda ADD COLUMN version INT;
ALTER TABLE excepciones_agenda ADD COLUMN created_at TIMESTAMPTZ;
ALTER TABLE excepciones_agenda ADD COLUMN updated_at TIMESTAMPTZ;

-- 2. Establecer valores por defecto para las nuevas columnas en registros existentes
-- (usamos NOW() para created_at y version 0 para empezar)

UPDATE usuarios SET version = 0, created_at = NOW() WHERE created_at IS NULL;
UPDATE profesionales SET version = 0, created_at = NOW() WHERE created_at IS NULL;
UPDATE pacientes SET version = 0, created_at = NOW() WHERE created_at IS NULL;
UPDATE citas SET version = 0, created_at = NOW() WHERE created_at IS NULL;
UPDATE disponibilidad_base SET version = 0, created_at = NOW() WHERE created_at IS NULL;
UPDATE excepciones_agenda SET version = 0, created_at = NOW() WHERE created_at IS NULL;

-- 3. Hacer las columnas NOT NULL después de poblarlas

ALTER TABLE usuarios ALTER COLUMN version SET NOT NULL;
ALTER TABLE usuarios ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE profesionales ALTER COLUMN version SET NOT NULL;
ALTER TABLE profesionales ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE pacientes ALTER COLUMN version SET NOT NULL;
ALTER TABLE pacientes ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE citas ALTER COLUMN version SET NOT NULL;
ALTER TABLE citas ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE disponibilidad_base ALTER COLUMN version SET NOT NULL;
ALTER TABLE disponibilidad_base ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE excepciones_agenda ALTER COLUMN version SET NOT NULL;
ALTER TABLE excepciones_agenda ALTER COLUMN created_at SET NOT NULL;


-- 4. Añadir las restricciones de clave foránea (FOREIGN KEY)

-- En 'profesionales', 'usuario_id' ahora apunta a 'usuarios'
ALTER TABLE profesionales
    ADD CONSTRAINT fk_profesionales_usuarios
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

-- En 'citas', 'paciente_id' y 'profesional_id' apuntan a sus respectivas tablas
ALTER TABLE citas
    ADD CONSTRAINT fk_citas_pacientes
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id);

ALTER TABLE citas
    ADD CONSTRAINT fk_citas_profesionales
    FOREIGN KEY (profesional_id) REFERENCES profesionales(id);

-- En 'disponibilidad_base', 'profesional_id' apunta a 'profesionales'
ALTER TABLE disponibilidad_base
    ADD CONSTRAINT fk_disponibilidad_profesionales
    FOREIGN KEY (profesional_id) REFERENCES profesionales(id);

-- En 'excepciones_agenda', 'profesional_id' apunta a 'profesionales'
ALTER TABLE excepciones_agenda
    ADD CONSTRAINT fk_excepciones_profesionales
    FOREIGN KEY (profesional_id) REFERENCES profesionales(id);

-- NOTA: No eliminamos las columnas antiguas de UUID directamente en la migración
-- para evitar pérdida de datos si algo sale mal. Una vez verificado, se podrían
-- eliminar en una migración futura (V5).
