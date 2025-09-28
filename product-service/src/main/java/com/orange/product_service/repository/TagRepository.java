package com.orange.product_service.repository;

import com.orange.product_service.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends BaseRepository<Tag> {
    
    Optional<Tag> findByName(String name);
    
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:name% ORDER BY t.name")
    List<Tag> findByNameContainingIgnoreCase(@Param("name") String name);
}
