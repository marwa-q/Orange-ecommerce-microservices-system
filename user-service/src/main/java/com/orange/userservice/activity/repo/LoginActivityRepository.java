package com.orange.userservice.activity.repo;

import com.orange.userservice.activity.entity.LoginActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginActivityRepository extends JpaRepository<LoginActivity, Long> { }
