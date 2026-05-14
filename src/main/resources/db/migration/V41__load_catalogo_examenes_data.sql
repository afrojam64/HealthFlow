-- V41__load_catalogo_examenes_data.sql (corregido)

-- =====================================================
-- CATÁLOGO DE EXÁMENES INDIVIDUALES (LABORATORIO)
-- =====================================================

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '903818', 'Colesterol Total', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903815', 'Colesterol HDL', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903817', 'Colesterol LDL', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903868', 'Triglicéridos', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903868', 'VLDL', 'LABORATORIO', 'SUERO', 'Suero', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '903867', 'AST / TGO', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903866', 'ALT / TGP', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903809', 'Bilirrubina Total', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903809', 'Bilirrubina Directa', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903833', 'Fosfatasa Alcalina', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903838', 'GGT', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903803', 'Albúmina', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903857', 'Proteínas Totales', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903844', 'LDH', 'LABORATORIO', 'SUERO', 'Suero', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '903856', 'BUN', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903895', 'Creatinina', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903801', 'Ácido Úrico', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '907106', 'Uroanálisis', 'LABORATORIO', 'ORINA', 'Orina', true),
(gen_random_uuid(), '907301', 'Microalbuminuria', 'LABORATORIO', 'ORINA', 'Orina', true),
(gen_random_uuid(), '907210', 'Proteinuria 24 horas', 'LABORATORIO', 'ORINA', 'Orina', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '904904', 'TSH Ultrasensible', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '904916', 'T3 Total', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '904917', 'T3 Libre', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '904922', 'T4 Total', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '904923', 'T4 Libre', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906932', 'Anti-TPO', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '904930', 'Tiroglobulina', 'LABORATORIO', 'SUERO', 'Suero', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '903841', 'Glucosa Ayunas', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903427', 'Hemoglobina Glicosilada HbA1c', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '904102', 'Insulina Basal', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903886', 'PTOG', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '904109', 'Péptido C', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '907106', 'Glucosuria', 'LABORATORIO', 'ORINA', 'Orina', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '902045', 'Tiempo de Protrombina PT', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '902049', 'PTT', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '902017', 'Fibrinógeno', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '902210', 'Recuento Plaquetas', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '902023', 'Dímero D', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '902052', 'Tiempo Sangría', 'LABORATORIO', 'SUERO', 'Suero', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '903861', 'Sodio', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903847', 'Potasio', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903813', 'Cloro', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903811', 'Calcio', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903846', 'Magnesio', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903835', 'Fósforo', 'LABORATORIO', 'SUERO', 'Suero', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '903864', 'Troponina I', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903821', 'CPK Total', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903819', 'CPK-MB', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '903850', 'Mioglobina', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '904501', 'BNP', 'LABORATORIO', 'SUERO', 'Suero', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, activo) VALUES
(gen_random_uuid(), '902210', 'Hemograma Completo', 'LABORATORIO', 'SUERO', true),
(gen_random_uuid(), '890201', 'Electrocardiograma', 'RADIOGRAFIA', 'ECG', true),
(gen_random_uuid(), '874101', 'RX Tórax', 'RADIOGRAFIA', 'RX', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '911017', 'Hemoclasificación', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906038', 'VDRL', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906249', 'VIH 1 y 2', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906317', 'HBsAg', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906408', 'IgG Toxoplasma', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906409', 'IgM Toxoplasma', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906350', 'IgG Rubéola', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906351', 'IgM Rubéola', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906120', 'Citomegalovirus IgG', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906121', 'Citomegalovirus IgM', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '907305', 'Urocultivo', 'LABORATORIO', 'ORINA', 'Orina', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, tipo_muestra, activo) VALUES
(gen_random_uuid(), '906910', 'Factor Reumatoideo', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906915', 'PCR', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '902204', 'VSG', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906903', 'ANA', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906904', 'Anti-DNA', 'LABORATORIO', 'SUERO', 'Suero', true),
(gen_random_uuid(), '906940', 'Anti-CCP', 'LABORATORIO', 'SUERO', 'Suero', true);

-- =====================================================
-- RADIOGRAFÍAS CORPORALES
-- =====================================================
INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '871001', 'RX Cráneo AP/Lateral', 'RADIOGRAFIA', 'RX', 'CRANEO', true),
(gen_random_uuid(), '871101', 'RX Senos Paranasales', 'RADIOGRAFIA', 'RX', 'CRANEO', true),
(gen_random_uuid(), '871102', 'RX Huesos Nasales', 'RADIOGRAFIA', 'RX', 'CRANEO', true),
(gen_random_uuid(), '871103', 'RX Maxilar Superior', 'RADIOGRAFIA', 'RX', 'MAXILAR', true),
(gen_random_uuid(), '871104', 'RX Mandíbula', 'RADIOGRAFIA', 'RX', 'MANDIBULA', true),
(gen_random_uuid(), '871105', 'RX ATM', 'RADIOGRAFIA', 'RX', 'ATM', true),
(gen_random_uuid(), '871106', 'RX Órbitas', 'RADIOGRAFIA', 'RX', 'CRANEO', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '875201', 'RX Columna Cervical', 'RADIOGRAFIA', 'RX', 'COLUMNA', true),
(gen_random_uuid(), '875203', 'RX Columna Dorsal', 'RADIOGRAFIA', 'RX', 'COLUMNA', true),
(gen_random_uuid(), '875205', 'RX Columna Lumbar', 'RADIOGRAFIA', 'RX', 'COLUMNA', true),
(gen_random_uuid(), '875207', 'RX Columna Sacra', 'RADIOGRAFIA', 'RX', 'COLUMNA', true),
(gen_random_uuid(), '875209', 'RX Columna Completa', 'RADIOGRAFIA', 'RX', 'COLUMNA', true),
(gen_random_uuid(), '875210', 'RX Escoliosis', 'RADIOGRAFIA', 'RX', 'COLUMNA', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '874101', 'RX Tórax PA', 'RADIOGRAFIA', 'RX', 'TORAX', true),
(gen_random_uuid(), '874102', 'RX Tórax Lateral', 'RADIOGRAFIA', 'RX', 'TORAX', true),
(gen_random_uuid(), '874103', 'RX Parrilla Costal', 'RADIOGRAFIA', 'RX', 'TORAX', true),
(gen_random_uuid(), '874104', 'RX Esternón', 'RADIOGRAFIA', 'RX', 'TORAX', true),
(gen_random_uuid(), '874105', 'RX Clavícula', 'RADIOGRAFIA', 'RX', 'TORAX', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '874201', 'RX Abdomen Simple', 'RADIOGRAFIA', 'RX', 'ABDOMEN', true),
(gen_random_uuid(), '874202', 'RX Abdomen Agudo', 'RADIOGRAFIA', 'RX', 'ABDOMEN', true),
(gen_random_uuid(), '874203', 'RX Pelvis', 'RADIOGRAFIA', 'RX', 'PELVIS', true),
(gen_random_uuid(), '874204', 'RX Caderas', 'RADIOGRAFIA', 'RX', 'CADERA', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '876001', 'RX Hombro', 'RADIOGRAFIA', 'RX', 'HOMBRO', true),
(gen_random_uuid(), '876002', 'RX Húmero', 'RADIOGRAFIA', 'RX', 'HUMERO', true),
(gen_random_uuid(), '876003', 'RX Codo', 'RADIOGRAFIA', 'RX', 'CODO', true),
(gen_random_uuid(), '876004', 'RX Antebrazo', 'RADIOGRAFIA', 'RX', 'ANTEBRAZO', true),
(gen_random_uuid(), '876005', 'RX Muñeca', 'RADIOGRAFIA', 'RX', 'MUNECA', true),
(gen_random_uuid(), '876101', 'RX Mano', 'RADIOGRAFIA', 'RX', 'MANO', true),
(gen_random_uuid(), '876102', 'RX Dedos Mano', 'RADIOGRAFIA', 'RX', 'MANO', true);

INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '876201', 'RX Fémur', 'RADIOGRAFIA', 'RX', 'FEMUR', true),
(gen_random_uuid(), '876301', 'RX Rodilla', 'RADIOGRAFIA', 'RX', 'RODILLA', true),
(gen_random_uuid(), '876302', 'RX Pierna Tibia/Peroné', 'RADIOGRAFIA', 'RX', 'PIERNA', true),
(gen_random_uuid(), '876303', 'RX Tobillo', 'RADIOGRAFIA', 'RX', 'TOBILLO', true),
(gen_random_uuid(), '876401', 'RX Pie', 'RADIOGRAFIA', 'RX', 'PIE', true),
(gen_random_uuid(), '876402', 'RX Calcáneo', 'RADIOGRAFIA', 'RX', 'PIE', true),
(gen_random_uuid(), '876403', 'RX Dedos Pie', 'RADIOGRAFIA', 'RX', 'PIE', true);

-- =====================================================
-- RADIOGRAFÍAS DENTALES
-- =====================================================
INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '887101', 'Radiografía Periapical', 'RADIOGRAFIA_DENTAL', 'RX', 'DIENTE', true),
(gen_random_uuid(), '887102', 'Radiografía Coronal (Bitewing)', 'RADIOGRAFIA_DENTAL', 'RX', 'DIENTE', true),
(gen_random_uuid(), '887103', 'Radiografía Oclusal', 'RADIOGRAFIA_DENTAL', 'RX', 'MAXILAR', true),
(gen_random_uuid(), '887104', 'Radiografía Panorámica', 'RADIOGRAFIA_DENTAL', 'RX', 'MAXILAR', true),
(gen_random_uuid(), '887105', 'Cefalometría', 'RADIOGRAFIA_DENTAL', 'RX', 'CRANEO', true),
(gen_random_uuid(), '887106', 'ATM Radiográfica', 'RADIOGRAFIA_DENTAL', 'RX', 'ATM', true),
(gen_random_uuid(), '887107', 'Serie Periapical Completa', 'RADIOGRAFIA_DENTAL', 'RX', 'MAXILAR', true),
(gen_random_uuid(), '887201', 'CBCT Maxilofacial', 'TOMOGRAFIA', 'CBCT', 'MAXILAR', true),
(gen_random_uuid(), '887202', 'CBCT Mandíbula', 'TOMOGRAFIA', 'CBCT', 'MANDIBULA', true),
(gen_random_uuid(), '887203', 'CBCT Maxilar', 'TOMOGRAFIA', 'CBCT', 'MAXILAR', true),
(gen_random_uuid(), '887204', 'CBCT ATM', 'TOMOGRAFIA', 'CBCT', 'ATM', true),
(gen_random_uuid(), '887205', 'CBCT Endodoncia', 'TOMOGRAFIA', 'CBCT', 'DIENTE', true);

-- =====================================================
-- ECOGRAFÍAS
-- =====================================================
INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '881201', 'Ecografía Abdominal', 'ECOGRAFIA', 'ECO', 'ABDOMEN', true),
(gen_random_uuid(), '881202', 'Ecografía Pélvica', 'ECOGRAFIA', 'ECO', 'PELVIS', true),
(gen_random_uuid(), '881203', 'Ecografía Obstétrica', 'ECOGRAFIA', 'ECO', 'PELVIS', true),
(gen_random_uuid(), '881204', 'Ecografía Renal', 'ECOGRAFIA', 'ECO', 'RINON', true),
(gen_random_uuid(), '881205', 'Ecografía Transvaginal', 'ECOGRAFIA', 'ECO', 'PELVIS', true),
(gen_random_uuid(), '881301', 'Doppler Venoso', 'ECOGRAFIA', 'DOPPLER', 'VASCULAR', true),
(gen_random_uuid(), '881302', 'Doppler Arterial', 'ECOGRAFIA', 'DOPPLER', 'VASCULAR', true),
(gen_random_uuid(), '881206', 'Ecografía Testicular', 'ECOGRAFIA', 'ECO', 'TESTICULO', true),
(gen_random_uuid(), '881207', 'Ecografía Tiroides', 'ECOGRAFIA', 'ECO', 'TIROIDES', true),
(gen_random_uuid(), '881208', 'Ecografía Mama', 'ECOGRAFIA', 'ECO', 'MAMA', true);

-- =====================================================
-- TOMOGRAFÍAS (TAC)
-- =====================================================
INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, requiere_contraste, activo) VALUES
(gen_random_uuid(), '879101', 'TAC Cráneo Simple', 'TOMOGRAFIA', 'TAC', 'CRANEO', false, true),
(gen_random_uuid(), '879102', 'TAC Cráneo Contrastado', 'TOMOGRAFIA', 'TAC', 'CRANEO', true, true),
(gen_random_uuid(), '879201', 'TAC Senos Paranasales', 'TOMOGRAFIA', 'TAC', 'CRANEO', false, true),
(gen_random_uuid(), '879301', 'TAC Tórax', 'TOMOGRAFIA', 'TAC', 'TORAX', false, true),
(gen_random_uuid(), '879401', 'TAC Abdomen', 'TOMOGRAFIA', 'TAC', 'ABDOMEN', false, true),
(gen_random_uuid(), '879402', 'TAC Abdomen/Pelvis', 'TOMOGRAFIA', 'TAC', 'ABDOMEN', false, true),
(gen_random_uuid(), '879501', 'TAC Columna Cervical', 'TOMOGRAFIA', 'TAC', 'COLUMNA', false, true),
(gen_random_uuid(), '879502', 'TAC Columna Lumbar', 'TOMOGRAFIA', 'TAC', 'COLUMNA', false, true),
(gen_random_uuid(), '879601', 'TAC Extremidad Superior', 'TOMOGRAFIA', 'TAC', 'EXTREMIDAD', false, true),
(gen_random_uuid(), '879602', 'TAC Extremidad Inferior', 'TOMOGRAFIA', 'TAC', 'EXTREMIDAD', false, true);

-- =====================================================
-- RESONANCIAS MAGNÉTICAS
-- =====================================================
INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, requiere_contraste, activo) VALUES
(gen_random_uuid(), '882101', 'RM Cerebro', 'RESONANCIA', 'RM', 'CRANEO', false, true),
(gen_random_uuid(), '882102', 'RM Silla Turca', 'RESONANCIA', 'RM', 'CRANEO', false, true),
(gen_random_uuid(), '882201', 'RM Columna Cervical', 'RESONANCIA', 'RM', 'COLUMNA', false, true),
(gen_random_uuid(), '882205', 'RM Columna Lumbar', 'RESONANCIA', 'RM', 'COLUMNA', false, true),
(gen_random_uuid(), '882301', 'RM Rodilla', 'RESONANCIA', 'RM', 'RODILLA', false, true),
(gen_random_uuid(), '882302', 'RM Hombro', 'RESONANCIA', 'RM', 'HOMBRO', false, true),
(gen_random_uuid(), '882303', 'RM Tobillo', 'RESONANCIA', 'RM', 'TOBILLO', false, true),
(gen_random_uuid(), '882401', 'RM Abdomen', 'RESONANCIA', 'RM', 'ABDOMEN', false, true),
(gen_random_uuid(), '882402', 'RM Pelvis', 'RESONANCIA', 'RM', 'PELVIS', false, true),
(gen_random_uuid(), '882403', 'RM Articulación Temporomandibular', 'RESONANCIA', 'RM', 'ATM', false, true);

-- =====================================================
-- MAMOGRAFÍA Y DENSITOMETRÍA
-- =====================================================
INSERT INTO catalogo_examenes (id, codigo_cups, nombre, categoria, modalidad, region_anatomica, activo) VALUES
(gen_random_uuid(), '883101', 'Mamografía Bilateral', 'MAMOGRAFIA', 'MAMO', 'MAMA', true),
(gen_random_uuid(), '883102', 'Mamografía Unilateral', 'MAMOGRAFIA', 'MAMO', 'MAMA', true),
(gen_random_uuid(), '884101', 'Densitometría Ósea', 'DENSITOMETRIA', 'DEXA', 'HUESO', true);

-- =====================================================
-- PERFILES CLÍNICOS
-- =====================================================
INSERT INTO perfiles_clinicos (id, nombre, descripcion, activo) VALUES
(gen_random_uuid(), 'Perfil lipídico', 'Evaluación de lípidos en sangre', true),
(gen_random_uuid(), 'Perfil hepático', 'Función hepática', true),
(gen_random_uuid(), 'Perfil renal', 'Función renal', true),
(gen_random_uuid(), 'Perfil tiroideo', 'Función tiroidea', true),
(gen_random_uuid(), 'Perfil glicémico', 'Control de glucosa', true),
(gen_random_uuid(), 'Perfil de coagulación', 'Evaluación de coagulación', true),
(gen_random_uuid(), 'Perfil electrolítico', 'Electrolitos séricos', true),
(gen_random_uuid(), 'Perfil cardíaco', 'Marcadores cardíacos', true),
(gen_random_uuid(), 'Perfil preoperatorio', 'Evaluación prequirúrgica', true),
(gen_random_uuid(), 'Perfil prenatal', 'Control prenatal', true),
(gen_random_uuid(), 'Perfil reumatológico', 'Enfermedades autoinmunes', true);

-- =====================================================
-- RELACIONES PERFIL-EXAMEN (usando subconsultas directas)
-- =====================================================

-- Perfil lipídico
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil lipídico' AND e.nombre IN ('Colesterol Total', 'Colesterol HDL', 'Colesterol LDL', 'Triglicéridos', 'VLDL');

-- Perfil hepático
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil hepático' AND e.nombre IN ('AST / TGO', 'ALT / TGP', 'Bilirrubina Total', 'Bilirrubina Directa', 'Fosfatasa Alcalina', 'GGT', 'Albúmina', 'Proteínas Totales', 'LDH');

-- Perfil renal
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil renal' AND e.nombre IN ('BUN', 'Creatinina', 'Ácido Úrico', 'Uroanálisis', 'Microalbuminuria', 'Proteinuria 24 horas');

-- Perfil tiroideo
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil tiroideo' AND e.nombre IN ('TSH Ultrasensible', 'T3 Total', 'T3 Libre', 'T4 Total', 'T4 Libre', 'Anti-TPO', 'Tiroglobulina');

-- Perfil glicémico
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil glicémico' AND e.nombre IN ('Glucosa Ayunas', 'Hemoglobina Glicosilada HbA1c', 'Insulina Basal', 'PTOG', 'Péptido C', 'Glucosuria');

-- Perfil de coagulación
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil de coagulación' AND e.nombre IN ('Tiempo de Protrombina PT', 'PTT', 'Fibrinógeno', 'Recuento Plaquetas', 'Dímero D', 'Tiempo Sangría');

-- Perfil electrolítico
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil electrolítico' AND e.nombre IN ('Sodio', 'Potasio', 'Cloro', 'Calcio', 'Magnesio', 'Fósforo');

-- Perfil cardíaco
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil cardíaco' AND e.nombre IN ('Troponina I', 'CPK Total', 'CPK-MB', 'Mioglobina', 'BNP');

-- Perfil preoperatorio
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil preoperatorio' AND e.nombre IN ('Hemograma Completo', 'Tiempo de Protrombina PT', 'PTT', 'Glucosa Ayunas', 'Creatinina', 'BUN', 'Electrocardiograma', 'RX Tórax');

-- Perfil prenatal
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil prenatal' AND e.nombre IN ('Hemoclasificación', 'VDRL', 'VIH 1 y 2', 'HBsAg', 'IgG Toxoplasma', 'IgM Toxoplasma', 'IgG Rubéola', 'IgM Rubéola', 'Citomegalovirus IgG', 'Citomegalovirus IgM', 'Uroanálisis', 'Urocultivo');

-- Perfil reumatológico
INSERT INTO perfil_examen (perfil_id, examen_id, orden)
SELECT p.id, e.id, 1
FROM perfiles_clinicos p, catalogo_examenes e
WHERE p.nombre = 'Perfil reumatológico' AND e.nombre IN ('Factor Reumatoideo', 'PCR', 'VSG', 'ANA', 'Anti-DNA', 'Ácido Úrico', 'Anti-CCP');