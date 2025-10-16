package com.tripgether.sns.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.sns.constant.SnsPlatform;
import com.tripgether.sns.constant.SnsStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sns extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID snsId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SnsPlatform platform;

    @Column(nullable = false, length = 500)
    private String contentUrl;

    @Column(length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String thumbnailUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SnsStatus status;

    @Column(length = 1000)
    private String analysisResult; // AI 분석 결과 JSON

    @Column(length = 1000)
    private String extractedPlaces; // 추출된 장소 정보 JSON

    @Column(length = 100)
    private String externalId; // SNS 플랫폼의 원본 콘텐츠 ID

    @Column(length = 500)
    private String tags; // 콘텐츠 태그 (쉼표로 구분)

    @Column(length = 50)
    private String contentType; // 이미지, 비디오, 텍스트 등
}
