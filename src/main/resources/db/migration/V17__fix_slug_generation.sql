-- Primero, eliminar caracteres especiales y reemplazar espacios por guiones
UPDATE profesionales SET slug =
                             LOWER(
                                     REGEXP_REPLACE(
                                             REGEXP_REPLACE(nombre_completo, '[^a-zA-Z0-9 ]', '', 'g'),
                                             '\s+', '-', 'g'
                                     )
                             )
WHERE slug IS NOT NULL;

-- Correcciones manuales específicas (ajusta según tus datos)
UPDATE profesionales SET slug = 'dra-laura-perez' WHERE nombre_completo = 'Dra. Laura Pérez';
UPDATE profesionales SET slug = 'dr-andres-gomez' WHERE nombre_completo = 'Dr. Andrés Gómez';
UPDATE profesionales SET slug = 'dra-sofia-ruiz' WHERE nombre_completo = 'Dra. Sofía Ruiz';
UPDATE profesionales SET slug = 'dr-camilo-torres' WHERE nombre_completo = 'Dr. Camilo Torres';
UPDATE profesionales SET slug = 'dra-valentina-diaz' WHERE nombre_completo = 'Dra. Valentina Díaz';

-- Asegurar que el slug sea único (si hay duplicados, añadir sufijo numérico)
-- Esto es opcional, solo si tienes nombres repetidos