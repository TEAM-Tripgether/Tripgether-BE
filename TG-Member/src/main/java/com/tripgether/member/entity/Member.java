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

  @Lob
  @Column(columnDefinition = "TEXT")
  private String profileImageUrl;

  @PrePersist
  protected void onCreate() {
      if (onboardingStatus == null) {
          onboardingStatus = MemberOnboardingStatus.NOT_STARTED;
      }
      if (memberRole == null) {
          memberRole = MemberRole.ROLE_USER;    //기본값
      }
  }

  public void updateProfileImage(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

}
