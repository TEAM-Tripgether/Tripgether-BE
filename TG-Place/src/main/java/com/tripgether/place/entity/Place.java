package com.tripgether.place.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Place extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;    //위도

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;   //경도

    @Column(length = 100)
    private String businessType;    //업종

    @Column(length = 50)
    private String phone;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;     //요약 설명

}
