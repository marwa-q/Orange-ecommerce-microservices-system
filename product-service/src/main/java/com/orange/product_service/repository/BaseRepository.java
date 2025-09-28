package com.orange.product_service.repository;

import com.orange.product_service.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, String> {
    
    List<T> findAll();
    
    Optional<T> findById(String id);
    
    List<T> findByIdIn(List<String> ids);
}
