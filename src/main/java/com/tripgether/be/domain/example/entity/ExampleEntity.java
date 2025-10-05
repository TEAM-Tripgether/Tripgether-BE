package com.tripgether.be.domain.example.entity;

import com.tripgether.be.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "examples")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExampleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isActive;

    @Builder
    public ExampleEntity(String name, String description, Boolean isActive) {
        this.name = name;
        this.description = description;
        this.isActive = isActive != null ? isActive : true;
    }

    public static ExampleEntity create(String name, String description) {
        return ExampleEntity.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .build();
    }
}

