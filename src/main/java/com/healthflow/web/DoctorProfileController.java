package com.healthflow.web;

import com.healthflow.domain.AsistentePermiso;
import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import com.healthflow.repo.AsistentePermisoRepository;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.AvatarService;
import com.healthflow.service.DomainException;
import com.healthflow.service.PermisoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Controller
@RequestMapping("/doctor")
public class DoctorProfileController {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsistentePermisoRepository permisoRepository;
    private final PermisoService permisoService;
    private final AvatarService avatarService;

    public DoctorProfileController(UserRepository userRepository,
                                   ProfessionalRepository professionalRepository,
                                   PasswordEncoder passwordEncoder,
                                   AsistentePermisoRepository permisoRepository,
                                   PermisoService permisoService,
                                   AvatarService avatarService) {
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
        this.passwordEncoder = passwordEncoder;
        this.permisoRepository = permisoRepository;
        this.permisoService = permisoService;
        this.avatarService = avatarService;
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private User getCurrentUser() {
        String username = getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException("Usuario no encontrado"));
    }

    private Professional getCurrentProfessional() {
        User user = getCurrentUser();
        return professionalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DomainException("No tienes un profesional asociado"));
    }

    @GetMapping("/perfil")
    public String perfil(Model model, HttpServletRequest request) {
        Professional professional = getCurrentProfessional();
        // Agregar el objeto professional completo al modelo (necesario para avatar)
        model.addAttribute("professional", professional);
        model.addAttribute("nombre", professional.getFullName());
        model.addAttribute("especialidad", professional.getSpecialty());
        // Token CSRF
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            model.addAttribute("csrfToken", token.getToken());
            model.addAttribute("csrfHeader", token.getHeaderName());
        }
        model.addAttribute("contenido", "doctor/perfil");
        return "fragments/layout";
    }

    @GetMapping("/configuracion")
    public String mostrarConfiguracion(Model model) {
        model.addAttribute("title", "Configuración - HealthFlow");
        model.addAttribute("username", getUsername());
        model.addAttribute("contenido", "doctor/configuracion");
        return "fragments/layout";
    }

    @PostMapping("/configuracion/cambiar-password")
    public String cambiarPassword(@RequestParam("currentPassword") String currentPassword,
                                  @RequestParam("newPassword") String newPassword,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las nuevas contraseñas no coinciden.");
            return "redirect:/doctor/configuracion";
        }
        User user = getCurrentUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/doctor/configuracion";
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMessage", "La contraseña actual es incorrecta.");
            return "redirect:/doctor/configuracion";
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Contraseña actualizada. Por favor, inicia sesión nuevamente.");
        return "redirect:/login?cambio=exitoso";
    }

    @PostMapping("/configuracion/crear-asistente")
    public String crearAsistente(@RequestParam("username") String username,
                                 @RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam(value = "permisos", required = false) List<String> permisos,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                throw new DomainException("El nombre de usuario ya existe");
            }
            if (userRepository.findByEmail(email).isPresent()) {
                throw new DomainException("El correo ya está registrado");
            }
            User asistente = new User();
            asistente.setUsername(username);
            asistente.setEmail(email);
            asistente.setPasswordHash(passwordEncoder.encode(password));
            asistente.setRole("ASISTENTE");
            asistente.setActive(true);
            asistente.setCreatedAt(OffsetDateTime.now());
            asistente.setUpdatedAt(OffsetDateTime.now());
            asistente = userRepository.save(asistente);
            UUID medicoId = getCurrentProfessional().getId();
            if (permisos != null && !permisos.isEmpty()) {
                for (String permiso : permisos) {
                    AsistentePermiso ap = new AsistentePermiso();
                    ap.setMedicoId(medicoId);
                    ap.setAsistenteId(asistente.getId());
                    ap.setPermiso(permiso);
                    ap.setConcedido(true);
                    permisoRepository.save(ap);
                }
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    "Asistente creado exitosamente. Se le han asignado " + (permisos != null ? permisos.size() : 0) + " permisos.");
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/doctor/configuracion";
    }

    @GetMapping("/configuracion/asistentes")
    @ResponseBody
    public List<Map<String, Object>> listarAsistentes() {
        User medicoUser = getCurrentUser();
        Professional medico = professionalRepository.findByUserId(medicoUser.getId())
                .orElseThrow(() -> new DomainException("Profesional no encontrado"));
        UUID medicoId = medico.getId();
        List<AsistentePermiso> registros = permisoRepository.findByMedicoId(medicoId);
        Map<UUID, List<String>> permisosPorAsistente = new HashMap<>();
        Map<UUID, String> nombresAsistentes = new HashMap<>();
        for (AsistentePermiso ap : registros) {
            UUID asId = ap.getAsistenteId();
            permisosPorAsistente.computeIfAbsent(asId, k -> new ArrayList<>()).add(ap.getPermiso());
            userRepository.findById(asId).ifPresent(u -> nombresAsistentes.put(asId, u.getUsername()));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (UUID asId : permisosPorAsistente.keySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", asId);
            map.put("nombre", nombresAsistentes.getOrDefault(asId, "Desconocido"));
            map.put("permisos", permisosPorAsistente.get(asId));
            result.add(map);
        }
        return result;
    }

    @PostMapping("/configuracion/asistentes/{asistenteId}/permisos")
    @ResponseBody
    public ResponseEntity<?> actualizarPermisos(@PathVariable("asistenteId") UUID asistenteId,
                                                @RequestBody List<String> permisos) {
        User medicoUser = getCurrentUser();
        Professional medico = professionalRepository.findByUserId(medicoUser.getId())
                .orElseThrow(() -> new DomainException("Profesional no encontrado"));
        permisoService.actualizarPermisos(medico.getId(), asistenteId, permisos);
        return ResponseEntity.ok(Map.of("message", "Permisos actualizados"));
    }

    @PostMapping("/perfil/avatar")
    public String subirAvatar(@RequestParam("avatar") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            Professional professional = professionalRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new DomainException("Profesional no encontrado"));
            String avatarUrl = avatarService.guardarAvatar(professional.getId(), file);
            professional.setAvatarUrl(avatarUrl);
            professionalRepository.save(professional);
            redirectAttributes.addFlashAttribute("successMessage", "Foto de perfil actualizada");
        } catch (DomainException | IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/doctor/perfil";
    }

    @PostMapping("/perfil/actualizar-biografia")
    public String actualizarBiografia(@RequestParam("biografia") String biografia,
                                      RedirectAttributes redirectAttributes) {
        try {
            if (biografia == null) biografia = "";
            if (biografia.length() > 500) {
                throw new DomainException("El relato no puede exceder los 500 caracteres.");
            }
            Professional professional = getCurrentProfessional();
            professional.setBiografia(biografia);
            professionalRepository.save(professional);
            redirectAttributes.addFlashAttribute("successMessage", "Relato profesional actualizado.");
        } catch (DomainException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/doctor/perfil";
    }
}