package com.tripgether.member.entity;

import com.tripgether.common.entity.BaseEntity;
import com.tripgether.common.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.UUID;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthCredential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = true)
    private LocalDateTime lastPasswordChangeAt;

    //BaseEntity를 상속받았기 때문에 createdAt, updatedAt 생략

}
