package com.tripgether.member.dto;

import com.tripgether.member.constant.MemberOnboardingStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TermAgreementResponse {
  private boolean completed;
  private MemberOnboardingStatus onboardingStatus;
}
