package com.tripgether.domain.example.service;

import com.tripgether.domain.example.dto.ExampleDto;
import com.tripgether.domain.example.entity.ExampleEntity;
import com.tripgether.domain.example.repository.ExampleRepository;
import com.tripgether.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExampleService {

    private final ExampleRepository exampleRepository;

    /**
     * 예시 데이터 생성
     * @param exampleDto 생성할 예시 데이터
     * @return 생성된 예시 데이터
     * @throws BusinessException 생성 실패 시
     */
    @Transactional
    public ExampleDto createExample(ExampleDto exampleDto) {

            // Entity 변환 및 저장
            ExampleEntity entity = ExampleEntity.builder()
                    .name(exampleDto.getName())
                    .build();

            ExampleEntity savedEntity = exampleRepository.save(entity);
            return ExampleDto.entityToDto(savedEntity);
    }


    public List<ExampleDto> getAllExamples() {
        List<ExampleEntity> entities = exampleRepository.findAll();
        return entities.stream()
                .map(ExampleDto::entityToDto)
                .collect(Collectors.toList());
    }
}
