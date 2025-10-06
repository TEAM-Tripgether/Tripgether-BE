package com.tripgether.domain.example.dto;

import com.tripgether.domain.example.entity.ExampleEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "예시 데이터 DTO")
public class ExampleDto {

    @Schema(description = "예시 ID", example = "1")
    private Long id;

    @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
    @Schema(description = "예시 이름", example = "샘플 예시", required = true)
    private String name;

    public static ExampleDto entityToDto (ExampleEntity entity) {
        return ExampleDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}

