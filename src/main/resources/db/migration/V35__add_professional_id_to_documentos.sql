ALTER TABLE documentos ADD COLUMN professional_id UUID;

-- Opcional: crear índice para búsquedas rápidas
CREATE INDEX idx_documentos_professional_id ON documentos(professional_id);

-- Nota: No agregamos restricción FK aún porque puede haber datos existentes sin professional_id.
-- Después de actualizar los registros antiguos (si los hay), podríamos agregar la FK:
-- ALTER TABLE documentos ADD CONSTRAINT fk_documentos_professional FOREIGN KEY (professional_id) REFERENCES profesionales(id);