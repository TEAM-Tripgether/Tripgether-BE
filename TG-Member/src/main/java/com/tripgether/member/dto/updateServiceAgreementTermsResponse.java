package com.tripgether.member.dto;

import com.tripgether.member.constant.OnboardingStep;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class updateServiceAgreementTermsResponse {
  private OnboardingStep currentStep;
  private String onboardingStatus;
}
