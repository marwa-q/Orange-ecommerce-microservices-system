package com.orange.userservice.admin.controller;

import com.orange.userservice.admin.dto.ChangeStatusRequest;
import com.orange.userservice.admin.service.AdminService;
import com.orange.userservice.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {

        this.adminService = adminService;
    }

    @GetMapping("/users")
    public Page<User> list(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size) {
        return adminService.listUsers(page, size);
    }

    @PatchMapping("/users/status/{id}")
    public void changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest req) {
        adminService.changeStatus(id, req);
    }

    @PatchMapping("/users/promote/{id}")
    public void promoteToAdmin(@PathVariable UUID id) {
        adminService.promoteToAdmin(id);
    }
}
