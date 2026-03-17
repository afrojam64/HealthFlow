-- Permitir valores nulos en las columnas motivo y evolucion
ALTER TABLE consultas_hc ALTER COLUMN motivo DROP NOT NULL;
ALTER TABLE consultas_hc ALTER COLUMN evolucion DROP NOT NULL;
