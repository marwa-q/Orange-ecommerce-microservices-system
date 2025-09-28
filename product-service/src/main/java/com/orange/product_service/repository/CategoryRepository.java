package com.orange.product_service.repository;

import com.orange.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends BaseRepository<Category> {
    
    Optional<Category> findByName(String name);
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name% ORDER BY c.name")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);
}
