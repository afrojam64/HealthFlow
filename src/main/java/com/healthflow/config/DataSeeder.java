package com.healthflow.config;

import com.healthflow.domain.*;
import com.healthflow.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProfessionalRepository professionalRepo;
    private final PatientRepository patientRepo;
    private final AvailabilityBaseRepository availabilityRepo;
    private final AgendaExceptionRepository exceptionRepo;
    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    private final boolean enabled;
    private final ZoneId zoneId;

    public DataSeeder(ProfessionalRepository professionalRepo,
                      PatientRepository patientRepo,
                      AvailabilityBaseRepository availabilityRepo,
                      AgendaExceptionRepository exceptionRepo,
                      AppointmentRepository appointmentRepo,
                      UserRepository userRepo,
                      PasswordEncoder passwordEncoder,
                      @Value("${healthflow.seed.enabled:true}") boolean enabled,
                      @Value("${healthflow.timezone:America/Bogota}") String tz) {
        this.professionalRepo = professionalRepo;
        this.patientRepo = patientRepo;
        this.availabilityRepo = availabilityRepo;
        this.exceptionRepo = exceptionRepo;
        this.appointmentRepo = appointmentRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.zoneId = ZoneId.of(tz);
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (!enabled) return;

        // Idempotente: si ya hay usuarios, no hace nada
        if (userRepo.count() > 0) {
            System.out.println("[DataSeeder] Ya existen datos, no se insertan de nuevo.");
            return;
        }

        System.out.println("[DataSeeder] Insertando datos de ejemplo...");

        // ===== 1) CREAR USUARIOS PRIMERO =====
        Map<String, User> users = new HashMap<>();

        // Roles deben coincidir con los que espera Spring Security (ROLE_ADMIN, ROLE_MEDICO, ROLE_ASISTENTE)
        users.put("admin", createUser("admin", "admin123", "admin@healthflow.local", "ADMIN"));
        users.put("dr.garcia", createUser("dr.garcia", "doctor123", "dr.garcia@healthflow.local", "MEDICO"));
        users.put("dra.perez", createUser("dra.perez", "doctor123", "dra.perez@healthflow.local", "MEDICO"));
        users.put("asistente", createUser("asistente", "asistente123", "asistente@healthflow.local", "ASISTENTE"));

        // Guardar usuarios
        userRepo.saveAll(users.values());
        System.out.println("[DataSeeder] Usuarios creados:");

        // ===== 2) CREAR PROFESIONALES Y VINCULARLOS CON USUARIOS =====
        List<Professional> professionals = Arrays.asList(
                createProfessional("Dra. Laura Pérez", "RM-10001", "Medicina General"),
                createProfessional("Dr. Andrés Gómez", "RM-10002", "Medicina Interna"),
                createProfessional("Dra. Sofía Ruiz", "RM-10003", "Pediatría"),
                createProfessional("Dr. Camilo Torres", "RM-10004", "Ortopedia"),
                createProfessional("Dra. Valentina Díaz", "RM-10005", "Ginecología")
        );

        // Vincular usuarios a profesionales
        professionals.get(0).setUser(users.get("dra.perez")); // Dra. Laura Pérez -> dra.perez
        professionals.get(1).setUser(users.get("dr.garcia")); // Dr. Andrés Gómez -> dr.garcia
        // Los demás profesionales no tienen usuario asociado (solo existen como entidad)

        professionalRepo.saveAll(professionals);
        System.out.println("[DataSeeder] Profesionales creados: " + professionals.size());

        // ===== 3) CREAR PACIENTES =====
        List<Patient> patients = Arrays.asList(
                createPatient("CC", "100000001", "Juan", "Carlos", "Ríos", "Gómez",
                        LocalDate.of(1995, 4, 12), "M", "11001", "juan.rios@mail.com", "3001234567"),
                createPatient("CC", "100000002", "María", null, "López", "Martínez",
                        LocalDate.of(1992, 8, 21), "F", "11001", "maria.lopez@mail.com", "3002223333"),
                createPatient("CC", "100000003", "Pedro", null, "Ramírez", null,
                        LocalDate.of(1988, 2, 2), "M", "05001", "pedro.ramirez@mail.com", "3011112222"),
                createPatient("CC", "100000004", "Luisa", "Fernanda", "Castro", null,
                        LocalDate.of(2000, 11, 10), "F", "76001", "luisa.castro@mail.com", "3024445555"),
                createPatient("CC", "100000005", "Ana", null, "Jiménez", "Pardo",
                        LocalDate.of(1985, 6, 30), "F", "13001", "ana.jimenez@mail.com", "3037778888")
        );
        patientRepo.saveAll(patients);
        System.out.println("[DataSeeder] Pacientes creados: " + patients.size());

        // ===== 4) CREAR DISPONIBILIDAD BASE =====
        int availabilityCount = 0;
        for (Professional p : professionals) {
            // Lunes a Viernes (1-5)
            for (int dayOfWeek = 1; dayOfWeek <= 5; dayOfWeek++) {
                // Mañana: 9:00 - 12:00
                availabilityRepo.save(createAvailability(p, dayOfWeek, LocalTime.of(9, 0), LocalTime.of(12, 0)));
                // Tarde: 14:00 - 17:00
                availabilityRepo.save(createAvailability(p, dayOfWeek, LocalTime.of(14, 0), LocalTime.of(17, 0)));
                availabilityCount += 2;
            }
        }
        System.out.println("[DataSeeder] Disponibilidad base creada: " + availabilityCount + " registros");

        // ===== 5) CREAR EXCEPCIONES =====
        LocalDate tomorrow = LocalDate.now(zoneId).plusDays(1);
        LocalDate nextWeek = LocalDate.now(zoneId).plusDays(7);

        // Bloqueo para Dra. Laura Pérez mañana de 10-11
        exceptionRepo.save(createException(
                professionals.get(0),
                tomorrow,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                ExceptionType.BLOQUEO
        ));

        // Extra para Dr. Andrés Gómez mañana de 18-19
        exceptionRepo.save(createException(
                professionals.get(1),
                tomorrow,
                LocalTime.of(18, 0),
                LocalTime.of(19, 0),
                ExceptionType.EXTRA
        ));

        // Bloqueo todo el día para Dra. Sofía Ruiz el próximo sábado
        exceptionRepo.save(createException(
                professionals.get(2),
                nextWeek.with(DayOfWeek.SATURDAY),
                null, null,
                ExceptionType.BLOQUEO
        ));

        System.out.println("[DataSeeder] Excepciones creadas");

        // ===== 6) CREAR CITAS =====
        OffsetDateTime baseTime = OffsetDateTime.now(zoneId)
                .plusDays(1)
                .withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        int appointmentCount = 0;

        // Cita 1: Pendiente
        createAppointment(
                professionals.get(0),
                patients.get(0),
                baseTime,
                AppointmentStatus.PENDIENTE
        );
        appointmentCount++;

        // Cita 2: Confirmada
        createAppointment(
                professionals.get(0),
                patients.get(1),
                baseTime.plusHours(2),
                AppointmentStatus.CONFIRMADA
        );
        appointmentCount++;

        // Cita 3: Pendiente
        createAppointment(
                professionals.get(1),
                patients.get(2),
                baseTime.plusDays(1),
                AppointmentStatus.PENDIENTE
        );
        appointmentCount++;

        // Cita 4: Cancelada
        createAppointment(
                professionals.get(1),
                patients.get(3),
                baseTime.plusDays(1).plusHours(2),
                AppointmentStatus.CANCELADA
        );
        appointmentCount++;

        // Cita 5: Atendida (con recordatorio enviado)
        Appointment attended = createAppointment(
                professionals.get(2),
                patients.get(4),
                baseTime.minusDays(1),
                AppointmentStatus.ATENDIDA
        );
        attended.setReminderSentAt(OffsetDateTime.now(zoneId).minusDays(2));
        appointmentRepo.save(attended);
        appointmentCount++;

        System.out.println("[DataSeeder] Citas creadas: " + appointmentCount);

        // ===== 7) MOSTRAR RESUMEN =====
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ DATASEEDER COMPLETADO EXITOSAMENTE");
        System.out.println("=".repeat(60));

        System.out.println("\n📊 ESTADÍSTICAS:");
        System.out.println("  - Usuarios: " + userRepo.count());
        System.out.println("  - Profesionales: " + professionalRepo.count());
        System.out.println("  - Pacientes: " + patientRepo.count());
        System.out.println("  - Citas: " + appointmentRepo.count());

        System.out.println("\n🔑 CREDENCIALES DE ACCESO:");
        System.out.println("  ADMINISTRADOR:");
        System.out.println("    Usuario: admin");
        System.out.println("    Password: admin123");
        System.out.println("    Rol: ADMIN");
        System.out.println();
        System.out.println("  MÉDICOS:");
        System.out.println("    Usuario: dr.garcia");
        System.out.println("    Password: doctor123");
        System.out.println("    Rol: MEDICO (asociado a: Dr. Andrés Gómez)");
        System.out.println();
        System.out.println("    Usuario: dra.perez");
        System.out.println("    Password: doctor123");
        System.out.println("    Rol: MEDICO (asociado a: Dra. Laura Pérez)");
        System.out.println();
        System.out.println("  ASISTENTE:");
        System.out.println("    Usuario: asistente");
        System.out.println("    Password: asistente123");
        System.out.println("    Rol: ASISTENTE");

        System.out.println("\n🌐 URLS PARA AGENDAR:");
        for (Professional p : professionals) {
            System.out.println("  http://localhost:8080/public/book/" + p.getId() +
                    "  (" + p.getFullName() + ")");
        }
        System.out.println("=".repeat(60));
    }

    // ===== HELPER METHODS =====

    private User createUser(String username, String rawPassword, String email, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setRole(role); // Spring Security espera roles sin prefijo ROLE_ (lo agregamos en SecurityConfig)
        user.setActive(true);
        return user;
    }

    private Professional createProfessional(String fullName, String medicalRegistry, String specialty) {
        Professional professional = new Professional();
        professional.setFullName(fullName);
        professional.setMedicalRegistry(medicalRegistry);
        professional.setSpecialty(specialty);
        return professional;
    }

    private Patient createPatient(String docType, String docNumber, String firstName, String middleName,
                                  String lastName, String secondLastName, LocalDate birthDate,
                                  String sex, String municipalityCode, String email, String phone) {
        Patient patient = new Patient();
        patient.setDocType(docType);
        patient.setDocNumber(docNumber);
        patient.setFirstName(firstName);
        patient.setMiddleName(middleName);
        patient.setLastName(lastName);
        patient.setSecondLastName(secondLastName);
        patient.setBirthDate(birthDate);
        patient.setSex(sex);
        patient.setMunicipalityCode(municipalityCode);
        patient.setEmail(email);
        patient.setPhone(phone);
        return patient;
    }

    private AvailabilityBase createAvailability(Professional professional, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
        AvailabilityBase availability = new AvailabilityBase();
        availability.setProfessional(professional);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(startTime);
        availability.setEndTime(endTime);
        return availability;
    }

    private AgendaException createException(Professional professional, LocalDate date, LocalTime startTime,
                                            LocalTime endTime, ExceptionType type) {
        AgendaException exception = new AgendaException();
        exception.setProfessional(professional);
        exception.setDate(date);
        exception.setStartTime(startTime);
        exception.setEndTime(endTime);
        exception.setType(type);
        return exception;
    }

    private Appointment createAppointment(Professional professional, Patient patient,
                                          OffsetDateTime dateTime, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setProfessional(professional);
        appointment.setPatient(patient);
        appointment.setDateTime(dateTime);
        appointment.setStatus(status);
        return appointmentRepo.save(appointment);
    }
}
