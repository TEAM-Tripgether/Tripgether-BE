package com.tripgether.place.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlaceStatus {
  ACTIVE,     // 활성화된 장소
  INACTIVE,   // 비활성화된 장소
  DELETED     // 삭제된 장소
}
