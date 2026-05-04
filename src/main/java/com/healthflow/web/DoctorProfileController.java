package com.healthflow.web;

import com.healthflow.domain.Professional;
import com.healthflow.domain.User;
import com.healthflow.repo.ProfessionalRepository;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctor")
public class DoctorProfileController {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor con inyección de PasswordEncoder
    public DoctorProfileController(UserRepository userRepository,
                                   ProfessionalRepository professionalRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
        this.passwordEncoder = passwordEncoder;
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
        model.addAttribute("nombre", professional.getFullName());
        model.addAttribute("especialidad", professional.getSpecialty());
        // Agregar token CSRF al modelo
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
        // 1. Validar que newPassword y confirmPassword coincidan
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las nuevas contraseñas no coinciden.");
            return "redirect:/doctor/configuracion";
        }

        // 2. Obtener el usuario autenticado (médico)
        User user = getCurrentUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/doctor/configuracion";
        }

        // 3. Verificar la contraseña actual
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("errorMessage", "La contraseña actual es incorrecta.");
            return "redirect:/doctor/configuracion";
        }

        // 4. Encriptar y guardar nueva contraseña
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 5. Redirigir al login con mensaje de éxito
        redirectAttributes.addFlashAttribute("successMessage", "Contraseña actualizada. Por favor, inicia sesión nuevamente.");
        return "redirect:/login?cambio=exitoso";
    }
}