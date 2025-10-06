package com.orange.product_service.category.repo;

import com.orange.product_service.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAll();

    Optional<Category> findByUuid(UUID uuid);

    List<Category> findByUuidIn(List<UUID> uuids);

    Optional<Category> findByName(String name);
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name% ORDER BY c.name")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);
}
