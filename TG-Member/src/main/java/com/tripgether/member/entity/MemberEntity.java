package com.tripgether.member.entity;

import com.tripgether.common.entity.BaseEntity;
import com.tripgether.member.constant.MemberStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 255)
    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Builder
    public MemberEntity(String email, String nickname, String profileImageUrl, MemberStatus status) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.status = status != null ? status : MemberStatus.ACTIVE;
    }

    public static MemberEntity create(String email, String nickname) {
        return MemberEntity.builder()
                .email(email)
                .nickname(nickname)
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
