package com.tripgether.member.entity;

import com.tripgether.common.constant.Role;
import com.tripgether.common.constant.SocialPlatform;
import com.tripgether.common.entity.BaseEntity;
import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.member.constant.MemberStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID memberId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 255)
    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SocialPlatform socialPlatform;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public Member(
            String email,
            String nickname,
            String profileImageUrl,
            MemberStatus status,
            SocialPlatform socialPlatform,
            Role role) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.status = status != null ? status : MemberStatus.ACTIVE;
        this.socialPlatform = socialPlatform != null ? socialPlatform : SocialPlatform.NORMAL;
        this.role = role != null ? role : Role.ROLE_USER;
    }

    public static Member create(String email, String nickname) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .status(MemberStatus.ACTIVE)
                .socialPlatform(SocialPlatform.NORMAL)
                .role(Role.ROLE_USER)
                .build();
    }

    public static Member createSocialMember(
            String email, String nickname, String profileImageUrl, SocialPlatform socialPlatform) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .status(MemberStatus.ACTIVE)
                .socialPlatform(socialPlatform)
                .role(Role.ROLE_USER)
                .build();
    }
}
