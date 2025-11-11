package com.tripgether.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor*/
public class InterestDto {
  private UUID id;
  private String name;

  // 기본 생성자
  public InterestDto(UUID id, String name) {
    this.id = id;
    this.name = name;
  }

  // Getter & Setter
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}