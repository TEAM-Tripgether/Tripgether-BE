package com.tripgether.place.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.place.constant.PlaceStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private UUID placeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String category;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaceStatus status;

    @Column(length = 100)
    private String externalId; // 네이버지도/구글맵 ID

    @Column(length = 20)
    private String externalSource; // 네이버지도, 구글맵 등
}
