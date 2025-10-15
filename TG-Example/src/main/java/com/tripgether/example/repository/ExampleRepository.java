package com.tripgether.domain.example.repository;

import com.tripgether.domain.example.entity.ExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleRepository extends JpaRepository<ExampleEntity, Long> {
}

