package com.orange.product_service.tag.repo;

import com.orange.product_service.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    List<Tag> findAll();
    
    Optional<Tag> findByUuid(UUID uuid);
    
    List<Tag> findByUuidIn(List<UUID> uuids);
    
    Optional<Tag> findByName(String name);
    
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:name% ORDER BY t.name")
    List<Tag> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT COUNT(t) FROM Tag t")
    long countAllTags();
}
