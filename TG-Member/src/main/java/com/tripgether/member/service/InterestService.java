package com.tripgether.member.service;

import com.tripgether.common.exception.CustomException;
import com.tripgether.common.exception.ErrorCodeBuilder;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.BusinessStatus;
import com.tripgether.common.exception.constant.ErrorMessageTemplate.Subject;
import com.tripgether.member.constant.InterestCategory;
import com.tripgether.member.dto.interest.response.GetAllInterestsResponse;
import com.tripgether.member.dto.interest.response.GetInterestByIdResponse;
import com.tripgether.member.dto.interest.response.GetInterestsByCategoryResponse;
import com.tripgether.member.entity.Interest;
import com.tripgether.member.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

  private final InterestRepository interestRepository;

  /**
   * 전체 관심사 목록 조회 (대분류별 그룹핑)
   * Redis 캐싱 적용 (TTL: 1시간)
   */
  @Cacheable(value = "interests", key = "'all'")
  public GetAllInterestsResponse getAllInterestsGroupedByCategory() {
    log.info("Fetching all interests grouped by category");

    List<Interest> interests = interestRepository.findAllOrderByCategoryAndName();

    // 카테고리별 그룹핑
    Map<InterestCategory, List<Interest>> groupedByCategory = interests.stream()
        .collect(Collectors.groupingBy(Interest::getCategory));

    // Response 변환
    return GetAllInterestsResponse.from(groupedByCategory);
  }

  /**
   * 특정 카테고리 관심사 조회
   */
  @Cacheable(value = "interests", key = "#category.name()")
  public GetInterestsByCategoryResponse getInterestsByCategory(InterestCategory category) {
    log.info("Fetching interests by category: {}", category);

    List<Interest> interests = interestRepository.findByCategory(category);

    return GetInterestsByCategoryResponse.from(interests);
  }

  /**
   * 관심사 ID로 조회
   */
  public GetInterestByIdResponse getInterestById(UUID interestId) {
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new CustomException(
            ErrorCodeBuilder.businessStatus(Subject.INTEREST, BusinessStatus.NOT_FOUND, HttpStatus.NOT_FOUND)
        ));

    return GetInterestByIdResponse.from(interest);
  }
}
