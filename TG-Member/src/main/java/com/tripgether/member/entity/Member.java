package com.tripgether.member.entity;

import com.tripgether.common.entity.SoftDeletableBaseEntity;
import com.tripgether.member.constant.MemberRole;
import com.tripgether.member.constant.MemberOnboardingStatus;
import com.tripgether.member.constant.MemberGender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
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

  //
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private MemberOnboardingStatus onboardingStatus = MemberOnboardingStatus.NOT_STARTED;

  @Column(nullable = false)
  @Builder.Default
  private Boolean tutorialEnabled = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private MemberRole memberRole = MemberRole.ROLE_USER;

  @Column(length = 500)
  private String profileImageUrl;

  // 필수 약관 동의
  @Column(nullable = false)
  @Builder.Default
  private boolean requiredAgreed = false;

  // 선택(마케팅) 약관 동의
  @Column(nullable = false)
  @Builder.Default
  private boolean marketingAgreed = false;

  // 버전이 있으면 여기에 기록 (예: "v1.0")
  @Column(length = 20)
  private String termsVersion;

  @PrePersist
  protected void onCreate() {
      if (onboardingStatus == null) {
          onboardingStatus = MemberOnboardingStatus.NOT_STARTED;
      }
      if (memberRole == null) {
          memberRole = MemberRole.ROLE_USER;    //기본값
      }
  }

}
