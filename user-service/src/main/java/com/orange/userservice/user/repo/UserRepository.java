package com.orange.userservice.user.repo;

import com.orange.userservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUuid(UUID uuid);

    @Query("SELECT u.email FROM User u WHERE u.uuid = :id")
    Optional<String> findEmailByUuid(@Param("id") UUID id);

}
