package org.userservice.user_service.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminDashboard() {
        return "Welcome, Admin! You have access to admin resources.";
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllUsers() {
        return "List of all users (admin-only feature)";
    }
}

