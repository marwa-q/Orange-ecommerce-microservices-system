package com.orange.userservice.admin.service;

import com.orange.userservice.admin.dto.ChangeStatusRequest;
import com.orange.userservice.common.dto.ApiResponse;
import com.orange.userservice.user.entity.Role;
import com.orange.userservice.user.entity.User;
import com.orange.userservice.user.repo.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepo;

    public AdminService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Page<User> listUsers(int page, int size) {
        return userRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public ApiResponse<Void> changeStatus(UUID id, ChangeStatusRequest req) {
        try {
            User u = userRepo.findByUuid(id).orElseThrow();
            u.setStatus(req.status());
            userRepo.save(u);
            return ApiResponse.success("user.status.updated.success");
        } catch (Exception e) {
            return ApiResponse.failure("user.status.update.failed");
        }
    }

    public ApiResponse<Void> promoteToAdmin(UUID id) {
        try {
            User u = userRepo.findByUuid(id).orElseThrow();
            u.setRole(Role.ADMIN);
            userRepo.save(u);
            return ApiResponse.success("user.promoted.success");
        } catch (Exception e) {
            return ApiResponse.failure("user.promotion.failed");
        }
    }
}
