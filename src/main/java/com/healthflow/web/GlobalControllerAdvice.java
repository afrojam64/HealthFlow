package com.healthflow.web;

import com.healthflow.domain.User;
import com.healthflow.repo.UserRepository;
import com.healthflow.service.PermisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private PermisoService permisoService;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("permisos")
    public List<String> getPermisos() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            User userFromPrincipal = (User) principal;
            // Obtener el usuario completo desde la base de datos para tener el ID
            User fullUser = userRepository.findByUsername(userFromPrincipal.getUsername()).orElse(null);
            if (fullUser != null && "ASISTENTE".equals(fullUser.getRole())) {
                return permisoService.getPermisosDeAsistente(fullUser.getId());
            }
        }
        return null;
    }
}