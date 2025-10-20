package com.tripgether.member.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.member.constant.MemberRole;
import com.tripgether.member.constant.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;
import com.tripgether.member.constant.MemberGender;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private MemberGender gender;      //null 허용

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus onboardingStatus;

    @Column(nullable = false)
    @Builder.Default
    private Boolean tutorialEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole memberRole;

    @PrePersist
    protected void onCreate() {
        if (onboardingStatus == null)
            onboardingStatus = MemberStatus.NOT_STARTED;
        if (memberRole == null)
            memberRole = MemberRole.GENERAL;    //기본값
    }

    public UUID getMemberId() {
        return id;
    }

    public String getNickname() {
        return email;
    }

}
