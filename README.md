# HealthFlow MVP (HU01–HU03) — Spring Boot + PostgreSQL

Este proyecto arranca el **núcleo técnico** del documento *HealthFlow*:
- HU01: Disponibilidad base (sin traslapes por día)
- HU02: Excepciones/Bloqueos (BLOQUEO/EXTRA)
- HU03: Portal público de agendamiento (crea paciente si no existe + crea cita con token UUID)

Incluye:
- Java 17, Spring Boot 3.x, Maven
- PostgreSQL + Flyway
- JPA/Hibernate + Bean Validation
- Restricción DB para **evitar doble agendamiento**: UNIQUE(profesional_id, fecha_hora)

## Requisitos
- Java 17
- Maven 3.9+
- PostgreSQL 14+

## Configuración
1) Crear DB:
```sql
CREATE DATABASE healthflow;
```
2) Copiar y ajustar variables en `src/main/resources/application.yml`.

## Ejecutar
```bash
mvn spring-boot:run
```

## Endpoints (MVP)
### Admin (sin UI todavía; REST para acelerar)
- `POST /api/admin/professionals` crea profesional
- `POST /api/admin/professionals/{id}/availability` crea disponibilidad base
- `POST /api/admin/professionals/{id}/exceptions` crea excepción (BLOQUEO/EXTRA)

### Público
- `GET /api/public/professionals/{id}/slots?date=YYYY-MM-DD` lista slots libres del día
- `POST /api/public/professionals/{id}/book` agenda (crea/actualiza paciente por documento)

## Decisiones MVP
- Duración fija de cita: 30 minutos (configurable con `healthflow.appointment.slotMinutes`).
- Zona horaria: `America/Bogota` (configurable con `healthflow.timezone`).
- Token de acceso: UUID generado al crear la cita (campo `token_acceso`).
