-- V39__create_periodontograma_tables.sql (corregido)
-- Tabla periodontogramas
CREATE TABLE periodontogramas (
                                  id UUID PRIMARY KEY,
                                  paciente_id UUID NOT NULL REFERENCES pacientes(id),
                                  cita_id UUID REFERENCES citas(id),
                                  profesional_id UUID NOT NULL REFERENCES profesionales(id),
                                  fecha_examen DATE NOT NULL,
                                  observaciones TEXT,
                                  mediciones_json JSONB NOT NULL,
    -- Diagnóstico (campos atómicos)
                                  diagnostico_base VARCHAR(50),
                                  subcategoria VARCHAR(100),
                                  stage VARCHAR(2),
                                  grade VARCHAR(1),
                                  extension VARCHAR(20),
                                  estabilidad VARCHAR(20),
                                  diagnostico_final_texto TEXT,
                                  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                  updated_at TIMESTAMP WITH TIME ZONE,
                                  version INTEGER
);

-- Tabla catálogo jerárquico de diagnósticos periodontales
CREATE TABLE periodontal_diagnosis_hierarchy (
                                                 id SERIAL PRIMARY KEY,
                                                 parent_id INT,
                                                 nivel INT NOT NULL,
                                                 grupo VARCHAR(100),                     -- Corregido: ahora soporta hasta 100 caracteres
                                                 subcategoria VARCHAR(100),
                                                 stage VARCHAR(3),
                                                 grade VARCHAR(1),
                                                 extension VARCHAR(20),
                                                 estabilidad VARCHAR(20),
                                                 descripcion TEXT,
                                                 cie10_code VARCHAR(10),
                                                 orden INT,
                                                 activo BOOLEAN DEFAULT true
);

-- Índices
CREATE INDEX idx_periodontogramas_paciente ON periodontogramas(paciente_id);
CREATE INDEX idx_periodontogramas_profesional ON periodontogramas(profesional_id);
CREATE INDEX idx_periodontogramas_cita ON periodontogramas(cita_id);
CREATE INDEX idx_periodontogramas_fecha ON periodontogramas(fecha_examen);

-- Datos iniciales de diagnóstico (jerarquía)
-- Nivel 1: Grupos principales
INSERT INTO periodontal_diagnosis_hierarchy (id, parent_id, nivel, grupo, orden) VALUES
                                                                                     (1, NULL, 1, 'Salud Gingival', 1),
                                                                                     (2, NULL, 1, 'Gingivitis', 2),
                                                                                     (3, NULL, 1, 'Periodontitis', 3),
                                                                                     (4, NULL, 1, 'Otras Condiciones Periodontales', 4),
                                                                                     (5, NULL, 1, 'Condiciones Periimplantarias', 5);

-- Nivel 2: Subcategorías para Salud Gingival
INSERT INTO periodontal_diagnosis_hierarchy (id, parent_id, nivel, grupo, subcategoria, descripcion, cie10_code, orden) VALUES
                                                                                                                            (10, 1, 2, 'Salud Gingival', 'Periodonto intacto', 'Encías sanas, sin pérdida de hueso ni inserción.', 'K00.9', 1),
                                                                                                                            (11, 1, 2, 'Salud Gingival', 'Periodonto reducido (no periodontitis)', 'Encía sana en un paciente que perdió hueso por otras causas (ej. ortodoncia o cirugía).', 'K00.9', 2),
                                                                                                                            (12, 1, 2, 'Salud Gingival', 'Periodonto reducido (paciente con periodontitis estable)', 'Paciente que tuvo la enfermedad, fue tratado y hoy está sano, pero con secuelas.', 'K00.9', 3);

-- Nivel 2: Subcategorías para Gingivitis
INSERT INTO periodontal_diagnosis_hierarchy (id, parent_id, nivel, grupo, subcategoria, descripcion, cie10_code, orden) VALUES
                                                                                                                            (20, 2, 2, 'Gingivitis', 'Asociada solo a biofilm', 'Inflamación causada únicamente por mala higiene.', 'K05.0', 1),
                                                                                                                            (21, 2, 2, 'Gingivitis', 'Mediada por factores de riesgo', 'Agravada por fumar, diabetes, embarazo o fármacos.', 'K05.1', 2),
                                                                                                                            (22, 2, 2, 'Gingivitis', 'Agrandamiento gingival', 'Crecimiento excesivo de la encía (común por algunos medicamentos).', 'K05.1', 3),
                                                                                                                            (23, 2, 2, 'Gingivitis', 'Enfermedades gingivales no inducidas por placa', 'Causadas por genética, infecciones (Virus/Hongos), alergias o traumas.', 'K05.1', 4);

-- Nivel 2: Placeholder para Periodontitis (agrupa las variantes de nivel 3)
INSERT INTO periodontal_diagnosis_hierarchy (id, parent_id, nivel, grupo, orden) VALUES (30, 3, 2, 'Periodontitis', 0);

-- Nivel 3: Estadios, Grados, Extensión y Estabilidad para Periodontitis
INSERT INTO periodontal_diagnosis_hierarchy (id, parent_id, nivel, grupo, stage, grade, extension, estabilidad, descripcion, orden) VALUES
                                                                                                                                        (100, 30, 3, 'Periodontitis', 'I', NULL, NULL, NULL, 'Pérdida de inserción de 1-2 mm (inicial)', 1),
                                                                                                                                        (101, 30, 3, 'Periodontitis', 'II', NULL, NULL, NULL, 'Pérdida de inserción de 3-4 mm (moderada)', 2),
                                                                                                                                        (102, 30, 3, 'Periodontitis', 'III', NULL, NULL, NULL, 'Pérdida ≥5 mm con riesgo de perder dientes (severa)', 3),
                                                                                                                                        (103, 30, 3, 'Periodontitis', 'IV', NULL, NULL, NULL, 'Pérdida masiva con riesgo de perder toda la dentición (muy severa)', 4),
                                                                                                                                        (104, 30, 3, 'Periodontitis', NULL, 'A', NULL, NULL, 'Progresión lenta', 5),
                                                                                                                                        (105, 30, 3, 'Periodontitis', NULL, 'B', NULL, NULL, 'Progresión moderada (default)', 6),
                                                                                                                                        (106, 30, 3, 'Periodontitis', NULL, 'C', NULL, NULL, 'Progresión rápida (fumadores, diabéticos)', 7),
                                                                                                                                        (107, 30, 3, 'Periodontitis', NULL, NULL, 'LOCALIZADA', NULL, '<30% de dientes afectados', 8),
                                                                                                                                        (108, 30, 3, 'Periodontitis', NULL, NULL, 'GENERALIZADA', NULL, '≥30% de dientes afectados', 9),
                                                                                                                                        (109, 30, 3, 'Periodontitis', NULL, NULL, NULL, 'ESTABLE', 'Enfermedad controlada', 10),
                                                                                                                                        (110, 30, 3, 'Periodontitis', NULL, NULL, NULL, 'INESTABLE', 'Progresión activa', 11);

-- Nivel 2: Otras Condiciones Periodontales
INSERT INTO periodontal_diagnosis_hierarchy (id, parent_id, nivel, grupo, subcategoria, descripcion, orden) VALUES
                                                                                                                (40, 4, 2, 'Otras Condiciones Periodontales', 'Abscesos periodontales agudos', 'Infecciones con pus de aparición rápida.', 1),
                                                                                                                (41, 4, 2, 'Otras Condiciones Periodontales', 'Lesiones endo-periodontales combinadas', 'Problema del nervio del diente que afecta la encía (o viceversa).', 2),
                                                                                                                (42, 4, 2, 'Otras Condiciones Periodontales', 'Condiciones mucogingivales / Recesiones', 'La encía se retrae dejando la raíz expuesta (Clases de Miller o Cairo).', 3),
                                                                                                                (43, 4, 2, 'Otras Condiciones Periodontales', 'Trauma oclusal por fuerzas excesivas', 'Daño al periodonto causado por mordida desalineada o bruxismo.', 4);

-- Nivel 2: Condiciones Periimplantarias
INSERT INTO periodontal_diagnosis_hierarchy (id, parent_id, nivel, grupo, subcategoria, descripcion, orden) VALUES
                                                                                                                (50, 5, 2, 'Condiciones Periimplantarias', 'Salud periimplantaria', 'Implante firme, sin sangrado ni pérdida de hueso.', 1),
                                                                                                                (51, 5, 2, 'Condiciones Periimplantarias', 'Mucositis periimplantaria', 'Equivalente a gingivitis: encía inflamada alrededor del implante sin pérdida ósea.', 2),
                                                                                                                (52, 5, 2, 'Condiciones Periimplantarias', 'Periimplantitis', 'Inflamación con pérdida del hueso que sostiene el implante (grave).', 3),
                                                                                                                (53, 5, 2, 'Condiciones Periimplantarias', 'Deficiencias de tejido (déficit de hueso/encía)', 'Falta de volumen de tejido tras una extracción.', 4);

-- Reiniciar secuencia (opcional)
SELECT setval('periodontal_diagnosis_hierarchy_id_seq', (SELECT MAX(id) FROM periodontal_diagnosis_hierarchy));