# 📋 HISTORIAS DE USUARIO - HealthFlow Completas
**Versión: 2.0 (Final - Con todas las funcionalidades)**  
**Fecha: 17 de Mayo de 2026**  
**Estado: Listo para Implementación**

---

## 📑 TABLA DE CONTENIDOS
1. [Épica 1: Gestión de Disponibilidad](#épica-1-gestión-de-disponibilidad)
2. [Épica 2: Portal de Pacientes y Agendamiento](#épica-2-portal-de-pacientes-y-agendamiento)
3. [Épica 3: Operación Clínica y Cumplimiento Legal](#épica-3-operación-clínica-y-cumplimiento-legal)
4. [Épica 4: Gestión de Documentos y Confirmación](#épica-4-gestión-de-documentos-y-confirmación)
5. [Épica 5: Generación de Remisiones y Órdenes (NUEVA)](#épica-5-generación-de-remisiones-y-órdenes-nueva)
6. [Épica 6: Prescripción Electrónica (NUEVA)](#épica-6-prescripción-electrónica-nueva)
7. [Épica 7: Reportes RIPS Avanzados (NUEVA)](#épica-7-reportes-rips-avanzados-nueva)
8. [Épica 8: Odontología Especializada (NUEVA)](#épica-8-odontología-especializada-nueva)
9. [Épica 9: Gestión de Asistentes y Permisos (NUEVA)](#épica-9-gestión-de-asistentes-y-permisos-nueva)

---

## 🎯 ÉPICA 1: GESTIÓN DE DISPONIBILIDAD

### HU01 - Configuración de Jornada Laboral Base
| Atributo | Valor |
|----------|-------|
| **ID** | HU01 |
| **Título** | Configuración de Jornada Laboral Base |
| **Actor** | Profesional de Salud / Asistente |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional de Salud / Asistente, quiero definir mis horarios de atención semanales, para que el sistema genere automáticamente los espacios de citas disponibles para los pacientes.

**Criterios de Aceptación:**
1. El sistema debe permitir seleccionar días de la semana (L-D)
2. Se deben definir horas de inicio y fin por cada jornada
3. El sistema debe validar que no existan traslapes de horarios en el mismo día
4. Los cambios deben impactar globalmente el calendario de agendamiento
5. Debe permitir editar y eliminar disponibilidades existentes
6. Se debe mostrar vista previa del calendario con las jornadas configuradas

**Endpoint:** `POST /admin/professionals/{id}/availability`

---

### HU02 - Gestión de Excepciones y Bloqueos de Agenda
| Atributo | Valor |
|----------|-------|
| **ID** | HU02 |
| **Título** | Gestión de Excepciones y Bloqueos de Agenda |
| **Actor** | Profesional de Salud / Asistente |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional de Salud / Asistente, quiero registrar fechas o bloques de tiempo específicos como no disponibles, para que se eviten agendamientos en momentos de cirugía, vacaciones o compromisos personales.

**Criterios de Aceptación:**
1. El sistema debe permitir seleccionar una fecha específica del calendario
2. Se debe poder bloquear el día completo o un rango de horas
3. Debe permitir especificar el tipo: BLOQUEO (ocupado) o EXTRA (jornada extendida)
4. Debe permitir ingresar un motivo (opcional)
5. El sistema debe mostrar alerta si el bloqueo coincide con citas ya programadas
6. El portal de agendamiento público debe ocultar estos espacios de inmediato
7. Se debe permitir crear excepciones recurrentes (ej: cada lunes festivo)

**Endpoint:** `POST /admin/professionals/{id}/exceptions`

---

## 🎯 ÉPICA 2: PORTAL DE PACIENTES Y AGENDAMIENTO

### HU03 - Agendamiento Autónomo de Pacientes
| Atributo | Valor |
|----------|-------|
| **ID** | HU03 |
| **Título** | Agendamiento Autónomo de Pacientes |
| **Actor** | Paciente (Nuevo o Recurrente) |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Paciente (Nuevo o Recurrente), quiero seleccionar un espacio disponible desde un portal web, para que pueda asegurar mi atención médica sin intermediación telefónica.

**Criterios de Aceptación:**
1. El sistema debe solicitar datos demográficos obligatorios (Documento, Nombres, Celular, Correo)
2. Se debe mostrar un calendario visual con espacios libres calculados en tiempo real
3. Al confirmar, el sistema debe registrar al paciente (si es nuevo) y crear la cita con estado PENDIENTE
4. Se debe generar un Token de Acceso UUID único para la cita
5. Debe validar que el paciente no tenga otra cita en el mismo rango horario
6. Se debe mostrar confirmación visual con número de cita y horario
7. Se debe permitir ver historial de citas previas (si el paciente es recurrente)

**Endpoints:** 
- `GET /api/public/professionals/{id}/slots?date=YYYY-MM-DD`
- `POST /api/public/professionals/{id}/book`

---

### HU04 - Notificación de Confirmación y Sincronización
| Atributo | Valor |
|----------|-------|
| **ID** | HU04 |
| **Título** | Notificación de Confirmación y Sincronización |
| **Actor** | Paciente |
| **Prioridad** | 🟠 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Paciente, quiero recibir un correo electrónico tras agendar, para que tenga un comprobante y pueda agregar el evento a mi calendario personal.

**Criterios de Aceptación:**
1. El envío del correo debe ser automático tras la confirmación en el portal
2. El correo debe incluir archivo adjunto en formato .ics compatible con iOS, Android y Windows
3. El cuerpo del correo debe contener el link de acceso seguro (vía Token) para gestión de documentos
4. Se debe incluir número de confirmación y código QR
5. Se debe enviar recordatorio 24h antes de la cita
6. Debe permitir cancelar/reprogramar desde el correo
7. Se debe registrar si el correo fue entregado exitosamente

**Service:** `NotificationService.sendAppointmentConfirmation()`

---

### HU09 - Cancelación y Reprogramación de Citas (NUEVA)
| Atributo | Valor |
|----------|-------|
| **ID** | HU09 |
| **Título** | Cancelación y Reprogramación de Citas |
| **Actor** | Paciente / Profesional |
| **Prioridad** | 🟠 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Paciente / Profesional, quiero poder cancelar o reprogramar una cita existente, para adaptarme a cambios de horario o emergencias.

**Criterios de Aceptación:**
1. El paciente debe poder cancelar vía token/email sin autenticación
2. El profesional debe poder cancelar desde su panel administrativo
3. Al cancelar, se debe liberar automáticamente el slot para otros pacientes
4. Se debe notificar al paciente de la cancelación vía email
5. Debe permitir reprogramar a otro horario disponible
6. Se debe registrar el motivo de cancelación (opcional)
7. Si cancela < 24h antes, debe registrarse como "cancelación tardía"
8. Se debe validar que no tenga otra cita en el mismo rango

**Endpoints:**
- `POST /paciente/citas/{id}/cancelar`
- `POST /paciente/citas/{id}/reprogramar`
- `POST /doctor/citas/{id}/cancelar`

---

## 🎯 ÉPICA 3: OPERACIÓN CLÍNICA Y CUMPLIMIENTO LEGAL

### HU05 - Registro de Evolución Médica (Historia Clínica)
| Atributo | Valor |
|----------|-------|
| **ID** | HU05 |
| **Título** | Registro de Evolución Médica (HC) |
| **Actor** | Profesional de Salud |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional de Salud, quiero registrar los hallazgos clínicos y diagnósticos de la consulta, para que se mantenga la continuidad asistencial y se cumpla con la normativa de Historia Clínica.

**Criterios de Aceptación:**
1. El registro debe estar anclado a una cita previamente agendada
2. Debe permitir la búsqueda y selección de diagnósticos mediante CIE-10
3. Campos obligatorios: "Motivo de consulta" y "Evolución" (texto libre)
4. Debe incluir enfermedad actual, examen físico y concepto/plan
5. Debe registrar diagnóstico principal y diagnósticos relacionados (hasta 3)
6. Una vez guardada, la consulta debe quedar bloqueada para ediciones posteriores
7. Debe permitir visualizar historia clínica anterior del paciente
8. Se debe registrar timestamp de creación y profesional que la genera

**Endpoints:**
- `POST /doctor/citas/{id}/guardar`
- `GET /doctor/citas/{id}/atender`

---

### HU10 - Evaluación Clínica Avanzada (NUEVA)
| Atributo | Valor |
|----------|-------|
| **ID** | HU10 |
| **Título** | Evaluación Clínica Avanzada con Catálogos |
| **Actor** | Profesional de Salud |
| **Prioridad** | 🟠 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional de Salud, quiero utilizar catálogos estándar (CIE-10, CUPS, Finalidad, Causa Externa) para que mis registros sean normalizados y compatibles con RIPS.

**Criterios de Aceptación:**
1. Búsqueda inteligente de diagnósticos CIE-10 (por código o descripción)
2. Selección de finalidad de consulta desde catálogo
3. Selección de causa externa (si aplica)
4. Selección de código CUPS de procedimiento
5. Registro de valores: valor servicio, cuota moderadora, copago
6. Validación que todos los campos sean compatibles con RIPS
7. Sugerencias automáticas basadas en historial del paciente
8. Visualización en tiempo real de categorías y relaciones

**Service:** `MedicalRecordService.saveMedicalRecord()`

---

## 🎯 ÉPICA 4: GESTIÓN DE DOCUMENTOS Y CONFIRMACIÓN

### HU07 - Gestión de Documentos vía Token Seguro
| Atributo | Valor |
|----------|-------|
| **ID** | HU07 |
| **Título** | Gestión de Documentos vía Token Seguro |
| **Actor** | Paciente / Profesional |
| **Prioridad** | 🟠 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Paciente, quiero subir mis exámenes previos y descargar mi fórmula médica mediante un link temporal, para que mi información esté protegida sin necesidad de crear una cuenta de usuario.

**Criterios de Aceptación:**
1. El acceso se valida mediante Token UUID + últimos 4 dígitos del documento
2. El sistema permite carga de archivos en formato PDF y JPG
3. Máximo 5MB por archivo, máximo 10 archivos
4. El médico visualiza documentos desde el módulo de consulta
5. El link expira automáticamente tras 30 días o después de la cita
6. Se debe registrar auditoría de acceso (quién, cuándo descargó)
7. Los documentos se encriptan en almacenamiento
8. Se puede descargar como ZIP si hay múltiples archivos

**Endpoints:**
- `POST /public/documentos/upload?token=...`
- `GET /public/documentos/descargar/{id}?token=...`
- `GET /doctor/citas/{id}/documentos`

**Service:** `DocumentoService.uploadDocument()`

---

### HU08 - Confirmación de Asistencia (Recordatorio)
| Atributo | Valor |
|----------|-------|
| **ID** | HU08 |
| **Título** | Confirmación de Asistencia (Recordatorio) |
| **Actor** | Paciente / Sistema |
| **Prioridad** | 🟠 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Sistema, quiero enviar recordatorios automáticos 24h antes de la cita, para que el paciente confirme o cancele, reduciendo la tasa de inasistencia.

**Criterios de Aceptación:**
1. Se envía automáticamente 24h antes de la cita programada
2. Correo incluye botones de acción: "Confirmar" y "Cancelar"
3. Al hacer clic, se actualiza automáticamente el estado de la cita
4. Si cancela, se libera el cupo en el portal público inmediatamente
5. Se permite cambiar fecha/hora desde el mismo enlace
6. Se registra si el paciente confirmó, canceló o no respondió
7. Se reenvía automáticamente 2h antes si no hubo respuesta
8. Se envía SMS/WhatsApp si el email no fue abierto

**Scheduler:** `ReminderScheduler.sendReminders()`

---

## 🎯 ÉPICA 5: GENERACIÓN DE REMISIONES Y ÓRDENES (NUEVA)

### HU11 - Generación de Remisión Médica
| Atributo | Valor |
|----------|-------|
| **ID** | HU11 |
| **Título** | Generación de Remisión Médica |
| **Actor** | Profesional de Salud |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional de Salud, quiero generar remisiones a especialistas con PDF firmado digitalmente, para que el paciente acceda a la atención especializada necesaria.

**Criterios de Aceptación:**
1. Desde la vista de atención, puedo generar remisión con 1 clic
2. Selecciono especialidad destino, prioridad (URGENTE/PREFERENTE/RUTINARIA) y motivo
3. El PDF incluye resumen clínico actual (enfermedad, examen, diagnóstico, medicamentos)
4. Se adjunta firma digital del profesional (si está disponible)
5. Se genera código QR para verificación de autenticidad
6. Se guarda copia en el expediente del paciente
7. Se registra en tabla de remisiones con token único
8. El paciente recibe link de descarga seguro vía email
9. Se puede reenviar el link sin regenerar el documento

**Endpoints:**
- `POST /doctor/citas/{id}/remision`
- `GET /public/remisiones/{token}/descargar`

**Service:** `RemisionService.generarRemision()`

---

### HU12 - Generación de Orden de Exámenes
| Atributo | Valor |
|----------|-------|
| **ID** | HU12 |
| **Título** | Generación de Orden de Exámenes |
| **Actor** | Profesional de Salud |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional de Salud, quiero crear órdenes de laboratorio/imagen con múltiples exámenes, para que el paciente acceda a los servicios diagnósticos necesarios.

**Criterios de Aceptación:**
1. Puedo seleccionar exámenes individuales o perfiles completos
2. Se puede agregar instrucciones específicas por examen (ayuno, preparación)
3. Se incluye código CUPS de cada examen
4. Se genera PDF con lista de exámenes, instrucciones y datos del paciente
5. Se registran todos los detalles en tabla orden_examen + detalles
6. Se guarda copia de seguridad del PDF
7. El paciente recibe link de descarga + instrucciones
8. Se puede modificar ANTES de la primera toma de muestra
9. Se integra con catálogo de ~100 exámenes precargados
10. Se permite agregar observaciones generales

**Endpoints:**
- `POST /doctor/citas/{id}/orden-examen`
- `GET /doctor/ordenes-examen/{id}`

**Service:** `OrdenExamenService.crearOrden()`

---

## 🎯 ÉPICA 6: PRESCRIPCIÓN ELECTRÓNICA (NUEVA)

### HU13 - Generación de Prescripción Médica (Fórmula)
| Atributo | Valor |
|----------|-------|
| **ID** | HU13 |
| **Título** | Generación de Prescripción Médica (Fórmula) |
| **Actor** | Profesional de Salud |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional de Salud, quiero generar fórmulas médicas digitales con medicamentos, dosis y frecuencias, para que el paciente pueda comprar medicinas en cualquier farmacia.

**Criterios de Aceptación:**
1. Desde la vista de atención, puedo arrastrar medicamentos a la fórmula
2. Cada medicamento incluye: nombre, dosis, frecuencia, cantidad y duración
3. Se buscan medicamentos en catálogo (autocomplete con ~1000 medicinas)
4. Se valida que las dosis estén dentro de rangos seguros
5. Se genera número de receta único (REC-YYYYMMDD-XXXXX)
6. El PDF incluye firma digital y código QR del profesional
7. Se registra fecha de emisión y fecha de expiración (30 días)
8. Se puede anular la receta si fue un error
9. Se genera token para verificación en farmacia (Resolución 1550)
10. Se envía vía email al paciente y se guarda en su expediente

**Endpoints:**
- `POST /doctor/citas/{id}/guardar` (con prescriptionJson)
- `GET /doctor/citas/{id}/prescription-pdf`
- `POST /doctor/citas/{id}/prescription-pdf` (JSON)

**Service:** `RecetaService.crearRecetaDesdeJson()`

---

### HU14 - Verificación de Recetas en Farmacia (NUEVA)
| Atributo | Valor |
|----------|-------|
| **ID** | HU14 |
| **Título** | Verificación de Recetas en Farmacia |
| **Actor** | Farmacéutico / Sistema |
| **Prioridad** | 🟢 BAJA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Farmacéutico, quiero verificar la autenticidad de una receta escaneando el código QR, para asegurar que es legítima y legal.

**Criterios de Aceptación:**
1. Portal público de verificación sin autenticación
2. Se ingresa número de receta o se escanea código QR
3. Se valida firma digital del profesional
4. Se muestra: paciente, profesional, medicamentos, fecha emisión/expiración
5. Se registra cada verificación (auditoría)
6. Se alerta si la receta está vencida o anulada
7. Se puede marcar como "dispensada" (opcional)
8. Se muestra estado: ACTIVA, PARCIALMENTE DISPENSADA, COMPLETAMENTE DISPENSADA, ANULADA

**Endpoints:**
- `GET /public/verify/receta`
- `POST /public/recetas/{numero}/verificar`

---

## 🎯 ÉPICA 7: REPORTES RIPS AVANZADOS (NUEVA)

### HU06 - Generación de Reporte RIPS (Resolución 2275)
| Atributo | Valor |
|----------|-------|
| **ID** | HU06 |
| **Título** | Generación de Reporte RIPS (Resolución 2275) |
| **Actor** | Administrador / Profesional |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Administrador / Profesional, quiero exportar los datos de las atenciones en formato JSON, para que pueda realizar el reporte obligatorio ante el Ministerio de Salud.

**Criterios de Aceptación:**
1. Se filtra por rango de fechas
2. Se valida que los datos del paciente estén completos (sexo, edad, municipio)
3. Se valida que cada consulta tenga diagnóstico principal y finalidad
4. Se incluyen procedimientos desde odontograma (si aplica)
5. El archivo cumple exactamente con esquema Resolución 2275
6. Se exporta en formato .json descargable
7. Se genera log de auditoría de cada exportación
8. Se permite varias exportaciones (por especialidad, por fecha, etc)
9. Se valida número de factura (FAC-, FEV-, REC- según tipo)
10. Se incluyen campos: valores, cuotas, copagos, códigos CUPS

**Endpoints:**
- `POST /doctor/reportes/generar-rips`
- `GET /doctor/reportes/rips/{id}/descargar`
- `GET /doctor/reportes/validar-citas?fecha_desde=...&fecha_hasta=...`

**Service:** `RipsService.generarRips()`

---

### HU15 - Validación Previa de Citas para RIPS (NUEVA)
| Atributo | Valor |
|----------|-------|
| **ID** | HU15 |
| **Título** | Validación Previa de Citas para RIPS |
| **Actor** | Profesional / Admin |
| **Prioridad** | 🟠 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional, quiero validar que mis citas cumplan con requisitos RIPS ANTES de generar el reporte, para evitar rechazos del ministerio.

**Criterios de Aceptación:**
1. Se muestra % de citas completas vs incompletas en el período
2. Se lista citas con errores (campos faltantes)
3. Se muestra qué campos faltan por cada cita
4. Se permite corregir desde la misma vista
5. Se genera reporte de validación descargable
6. Se alerta si hay menos de 100% de citas válidas
7. Se permite marcar como "no aplicable" para citas especiales
8. Se valida también datos del paciente (municipio, tipo doc, etc)

**Endpoint:** `GET /doctor/reportes/validar-citas`

**Service:** `RipsService.validarCitas()`

---

## 🎯 ÉPICA 8: ODONTOLOGÍA ESPECIALIZADA (NUEVA)

### HU16 - Registro de Odontograma Digital
| Atributo | Valor |
|----------|-------|
| **ID** | HU16 |
| **Título** | Registro de Odontograma Digital |
| **Actor** | Odontólogo |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Odontólogo, quiero registrar hallazgos en odontograma digital para cada diente, para que quede documentado el estado bucal y se generen códigos CUPS automáticamente.

**Criterios de Aceptación:**
1. Interfaz visual de 32 dientes (adulto) o 20 (infantil)
2. Se puede marcar: caries, restauración, endodoncia, exodoncia, implante, etc
3. Se registra cara del diente (oclusal, vestibular, lingual, mesial, distal)
4. Se automapean códigos CUPS según tipo de hallazgo
5. Se genera lista de procedimientos sugeridos
6. Se puede agregar observaciones por diente
7. Se guarda como JSON en tabla odontograma_hallazgos
8. Se integra con generación de RIPS (procedimientos)
9. Se puede imprimir odontograma como PDF
10. Se permite comparar odontogramas de consultas anteriores

**Template:** `doctor/odontograma.html` (49.4 KB)

**Service:** `OdontogramaService.procesarOdontograma()`

---

### HU17 - Evaluación Periodontal (Periodontograma)
| Atributo | Valor |
|----------|-------|
| **ID** | HU17 |
| **Título** | Evaluación Periodontal (Periodontograma) |
| **Actor** | Periodoncista / Odontólogo |
| **Prioridad** | 🔴 ALTA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Periodoncista / Odontólogo, quiero registrar evaluación periodontal detallada (profundidad bolsas, sangrado, movilidad) para documentar estado de encías.

**Criterios de Aceptación:**
1. Interfaz de tabla para cada diente (6 sitios por diente)
2. Registro de: profundidad de bolsa, sangrado, movilidad, recesión
3. Se clasifican diagnósticos periodontales: sano, gingivitis, periodontitis
4. Se aplica clasificación de Ramfjord
5. Se calcula score de sangrado y profundidad media
6. Se proponen tratamientos según diagnóstico
7. Se genera reporte en PDF
8. Se almacena en tabla periodontograma + jerarquía diagnóstica
9. Se integra con RIPS (códigos CUPS de periodoncia)
10. Se permite comparar con evaluaciones anteriores

**Template:** `doctor/periodontograma.html` (49.4 KB)

**Service:** `PeriodontogramaService.procesarPeriodontograma()`

---

## 🎯 ÉPICA 9: GESTIÓN DE ASISTENTES Y PERMISOS (NUEVA)

### HU18 - Gestión de Asistentes del Profesional
| Atributo | Valor |
|----------|-------|
| **ID** | HU18 |
| **Título** | Gestión de Asistentes del Profesional |
| **Actor** | Profesional / Admin |
| **Prioridad** | 🟠 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional, quiero crear asistentes y asignarles permisos sobre mis citas, para que puedan ayudar en tareas administrativas.

**Criterios de Aceptación:**
1. Se pueden crear múltiples asistentes por profesional
2. Se asignan permisos a nivel de cita: ver, editar, generar documentos
3. Se puede restringir a ciertos tipos de citas
4. Se registran accesos de asistentes (auditoría)
5. Se puede activar/desactivar asistente sin eliminar
6. Se valida que el asistente tenga cuenta de usuario
7. Se notifica al asistente de nuevas asignaciones
8. Se pueden revocar permisos en cualquier momento
9. El profesional siempre tiene permisos totales
10. Se registra quién hizo cambios en cada cita

**Endpoints:**
- `POST /doctor/asistentes`
- `PUT /doctor/asistentes/{id}/permisos`
- `DELETE /doctor/asistentes/{id}`

**Service:** `PermisoService.asignarPermisos()`

---

### HU19 - Asignación de Citas a Asistentes
| Atributo | Valor |
|----------|-------|
| **ID** | HU19 |
| **Título** | Asignación de Citas a Asistentes |
| **Actor** | Profesional / Asistente |
| **Prioridad** | 🟠 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Profesional, quiero asignar citas específicas a mis asistentes, para que puedan ayudar en pre-consulta, post-consulta o trámites administrativos.

**Criterios de Aceptación:**
1. Desde agenda, se puede arrastrar cita a asistente
2. Se define tipo de tarea: pre-consulta, documentos, facturación, post-consulta
3. Se pueden crear recordatorios para el asistente
4. El asistente ve citas asignadas en su panel
5. El asistente marca tareas como completadas
6. Se registra quién completó cada tarea y cuándo
7. Se puede desasignar en cualquier momento
8. Se notifica al asistente de nuevas asignaciones
9. Se registra en auditoría todas las asignaciones

**Endpoints:**
- `POST /doctor/citas/{id}/asignar-asistente`
- `GET /asistente/mis-tareas`
- `PUT /asistente/tareas/{id}/completar`

---

### HU20 - Auditoría y Trazabilidad de Acciones (NUEVA)
| Atributo | Valor |
|----------|-------|
| **ID** | HU20 |
| **Título** | Auditoría y Trazabilidad de Acciones |
| **Actor** | Admin / Profesional |
| **Prioridad** | 🟡 MEDIA |
| **Estado** | ✅ IMPLEMENTADA |

**Descripción:**  
Como Admin, quiero registrar todas las acciones en el sistema (consultas, cambios, descargas) para cumplir con normativas de trazabilidad y protección de datos.

**Criterios de Aceptación:**
1. Se registra: quién, qué, cuándo, dónde (IP), por qué
2. Se registran cambios a historias clínicas
3. Se registran accesos a documentos (descargas)
4. Se registran intentos fallidos de acceso
5. Se pueden filtrar auditorías por usuario, fecha, tipo de acción
6. Se exportan en CSV para análisis
7. Se retienen mínimo 3 años
8. Se valida que solo se modifique info permitida por ley
9. Se registran cambios de estado de cita
10. Se alertan acciones sospechosas (múltiples descargas, accesos anormales)

**Tabla:** `auditoria_acciones`

---

## 📊 RESUMEN POR ÉPICAS

| Épica | HU | Estado | Prioridad |
|-------|----|----|----------|
| **Gestión Disponibilidad** | HU01, HU02 | ✅ | 🔴 ALTA |
| **Portal Agendamiento** | HU03, HU04, HU09 | ✅ | 🔴 ALTA / 🟠 MEDIA |
| **Operación Clínica** | HU05, HU10 | ✅ | 🔴 ALTA |
| **Gestión Documentos** | HU07, HU08 | ✅ | 🟠 MEDIA |
| **Remisiones y Órdenes** | HU11, HU12 | ✅ | 🔴 ALTA |
| **Prescripción Electrónica** | HU13, HU14 | ✅ | 🔴 ALTA / 🟢 BAJA |
| **Reportes RIPS** | HU06, HU15 | ✅ | 🔴 ALTA / 🟠 MEDIA |
| **Odontología** | HU16, HU17 | ✅ | 🔴 ALTA |
| **Asistentes y Permisos** | HU18, HU19, HU20 | ✅ | 🟠 MEDIA / 🟡 MEDIA |

---

## 🎯 TOTAL: 20 HISTORIAS DE USUARIO

- **Épicas:** 9
- **Historias de Usuario:** 20
- **Implementadas:** 20 (100%)
- **En Desarrollo:** 0
- **Pendientes:** 0

---

## 📋 MAPEO DE COMPONENTES

### Controllers Relacionados
- `AdminController.java` → HU01, HU02, HU18, HU19, HU20
- `DoctorAppointmentAttentionController.java` → HU05, HU06, HU10, HU11, HU12, HU13
- `DoctorAgendaController.java` → HU01, HU02, HU19
- `PacienteAppointmentController.java` → HU03, HU04, HU09
- `DocumentoController.java` → HU07, HU11, HU12, HU13
- `OdontogramaController.java` → HU16
- `PeriodontogramaController.java` → HU17
- `RipsController.java` → HU06, HU15

### Services Relacionados
- `RemisionService.java` → HU11
- `OrdenExamenService.java` → HU12
- `RecetaService.java` → HU13
- `RipsService.java` → HU06, HU15
- `OdontogramaService.java` → HU16
- `PeriodontogramaService.java` → HU17
- `PermisoService.java` → HU18, HU19, HU20

### Templates Relacionados
- `doctor/atencion.html` (63.1 KB) → HU05, HU06, HU10, HU11, HU12, HU13
- `doctor/agenda.html` (48.4 KB) → HU01, HU02, HU19
- `doctor/odontograma.html` (49.4 KB) → HU16
- `doctor/periodontograma.html` (49.4 KB) → HU17
- `paciente/agendar.html` (14.9 KB) → HU03
- `paciente/mis-citas.html` (12.6 KB) → HU04, HU09

---

## 🔄 FLUJO DE USUARIO TÍPICO

1. **Paciente** (HU03) → Agenda cita en portal público
2. **Sistema** (HU04) → Envía confirmación + recordatorio (HU08)
3. **Profesional** (HU01, HU02) → Gestiona su disponibilidad y excepciones
4. **Profesional** (HU05, HU10) → Realiza atención y registra historia clínica
5. **Profesional** (HU11, HU12, HU13) → Genera remisión, orden y receta
6. **Profesional** (HU16, HU17) → Si es odontólogo, registra odontograma/periodontograma
7. **Profesional** (HU06, HU15) → Exporta reporte RIPS
8. **Farmacéutico** (HU14) → Verifica receta antes de dispensar

---

## ✅ CRITERIOS DE ACEPTACIÓN CUMPLIDOS

Todas las 20 HU cumplen con:
- ✅ Descripción clara
- ✅ Criterios de aceptación específicos
- ✅ Prioridad definida
- ✅ Estado de implementación
- ✅ Endpoints/Services asociados
- ✅ Componentes del sistema

---

**Documento generado:** 17/05/2026  
**Versión:** 2.0 Final  
**Estado:** Listo para Desarrollo/Mantenimiento
