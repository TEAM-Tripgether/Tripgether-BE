package com.tripgether.place.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.member.entity.Member;

import com.tripgether.place.constant.FolderVisibility;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Folder extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FolderVisibility visibility;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String shareLink;       //공유할 수 있는 토큰/링크

    //createdAt, updatedAt 생략

    @PrePersist
    protected void onCreate() {
        if (name == null)
            name = "제목 없음";
        if (visibility == null)
            visibility = FolderVisibility.PRIVATE;    //기본값
    }

}
