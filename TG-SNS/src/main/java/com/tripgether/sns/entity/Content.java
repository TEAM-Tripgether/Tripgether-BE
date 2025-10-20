package com.tripgether.sns.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.sns.constant.ContentPlatform;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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
public class Content extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID Id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentPlatform platform;

    @Column(nullable = false, length = 255)
    private String platformUploader;    //컨텐츠 업로더 계정 이름

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String caption;         //게시물 본문

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String thumbnailUrl;    //대표 썸네일

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalUrl;     //원본 url

    @Column(nullable = false, length = 500)
    private String title;       //AI가 생성한 제목

    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;     //AI가 생성한 요약

    @Column
    private LocalDateTime lastCheckedAt;    //마지막 조회 시간

    //createdAt, updatedAt 생략

}
