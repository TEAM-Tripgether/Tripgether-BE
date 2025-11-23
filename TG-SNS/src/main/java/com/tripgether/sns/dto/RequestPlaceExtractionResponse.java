package com.tripgether.sns.dto;

import com.tripgether.common.constant.ContentStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPlaceExtractionResponse {

  private UUID contentId;

  private ContentStatus status;
}
