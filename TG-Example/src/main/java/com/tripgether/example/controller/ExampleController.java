package com.tripgether.domain.example.controller;

import com.tripgether.domain.example.dto.ExampleDto;
import com.tripgether.domain.example.service.ExampleService;
import com.tripgether.global.exception.ErrorCodeBuilder;
import com.tripgether.global.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tripgether.global.constant.MessageComponent.Subject;
import com.tripgether.global.constant.MessageComponent.Action;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/examples")
@Tag(name = "예시 관리", description = "공통 에러/성공 코드 사용 예시를 보여주는 API")
public class ExampleController {

    private final ExampleService exampleService;

    @Operation(summary = "예시 데이터 생성",
            description = "새로운 예시 데이터를 생성합니다.")
    @PostMapping
    public ResponseEntity<ExampleDto> createExample(@RequestBody ExampleDto exampleDto) {
        ExampleDto dto = exampleService.createExample(exampleDto);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "예시 데이터 목록 조회",
            description = "모든 예시 데이터를 조회합니다. 공통 에러 코드 사용 예시입니다.")
    @GetMapping("")
    public ResponseEntity<List<ExampleDto>> getAllExamples() {
        List<ExampleDto> dtos = exampleService.getAllExamples();

        // 에러 코드 생성 및 예외 처리 예시
        // ErrorCodeBuilder를 사용하여 에러 코드 생성 후 CustomException를 통해 예외 발생
        if (dtos.isEmpty()) {
            ErrorCodeBuilder errorCode = ErrorCodeBuilder
                    .fail(Subject.EXAMPLE, Action.FIND, HttpStatus.NOT_FOUND);
            throw new CustomException(errorCode);
        }

        return ResponseEntity.ok(dtos);
    }
}
