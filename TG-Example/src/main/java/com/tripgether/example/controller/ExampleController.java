package com.tripgether.domain.example.controller;

import com.tripgether.domain.example.dto.ExampleDto;
import com.tripgether.domain.example.service.ExampleService;
import com.tripgether.global.constant.ErrorCodeContainer;
import com.tripgether.global.constant.ErrorCodeFactory;
import com.tripgether.global.constant.SuccessCodeContainer;
import com.tripgether.global.constant.SuccessCodeFactory;
import com.tripgether.global.exception.CustomException;
import com.tripgether.global.exception.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
            description = "새로운 예시 데이터를 생성합니다. 공통 성공/에러 코드 사용 예시입니다.")
    @PostMapping
    public SuccessResponse<ExampleDto> createExample(@RequestBody ExampleDto exampleDto) {
        ExampleDto dto = exampleService.createExample(exampleDto);

        // 성공 코드 생성 예시
        // SuccessCodeFactory를 사용하여 성공 코드 생성 후 SuccessCodeContainer로 변환
        // SuccessResponse를 통해 응답 반환
        SuccessCodeContainer successCode = SuccessCodeFactory.created(Subject.EXAMPLE);
        return SuccessResponse.getResponse(successCode, dto);
    }

    @Operation(summary = "예시 데이터 목록 조회",
            description = "모든 예시 데이터를 조회합니다. 공통 성공/에러 코드 사용 예시입니다.")
    @GetMapping("")
    public SuccessResponse<List<ExampleDto>> getAllExamples() {
        List<ExampleDto> dtos = exampleService.getAllExamples();

        // 에러 코드 생성 및 예외 처리 예시
        // ErrorCodeFactory를 사용하여 에러 코드 생성 후 ErrorCodeContainer로 변환
        // CustomException를 통해 예외 발생
        if (dtos.isEmpty()) {
            ErrorCodeContainer errorCode = ErrorCodeFactory
                    .fail(Subject.EXAMPLE, Action.FIND, HttpStatus.NOT_FOUND);
            throw new CustomException(errorCode);
        }

        SuccessCodeContainer successCode = SuccessCodeFactory
                .retrieved(Subject.EXAMPLE);
        return SuccessResponse.getResponse(successCode, dtos);
    }
}
