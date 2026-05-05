package com.healthflow.web;

import com.healthflow.domain.User;
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

    @ModelAttribute("permisos")
    public List<String> getPermisos() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            if ("ASISTENTE".equals(user.getRole())) {
                return permisoService.getPermisosDeAsistente(user.getId());
            }
        }
        // Para médicos o admin, devolvemos null (significa que no hay restricción, mostrar todo)
        return null;
    }
}