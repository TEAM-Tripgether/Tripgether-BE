package com.tripgether.application.service;

import com.github.javafaker.Faker;
import com.tripgether.application.dto.TestRequest;
import com.tripgether.application.dto.TestResponse;
import com.tripgether.common.constant.ContentStatus;
import com.tripgether.sns.constant.ContentPlatform;
import com.tripgether.sns.entity.Content;
import com.tripgether.sns.repository.ContentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestService {

  private final ContentRepository contentRepository;
  private static final Faker faker = new Faker(Locale.of("ko", "KR"));

  @Transactional
  public TestResponse createMockContent(TestRequest testRequest) {
    // contentCount 기본값 1개로 설정
    int contentCount = testRequest.getContentCount();
    if (contentCount <= 0) {
      contentCount = 1;
    }

    List<ContentPlatform> platforms = Arrays.asList(ContentPlatform.values());
    List<Content> savedContents = new ArrayList<>();

    // contentCount만큼 Content 생성
    for (int i = 0; i < contentCount; i++) {
      // 각 Content마다 랜덤 플랫폼 선택
      ContentPlatform randomPlatform = platforms.get(faker.random().nextInt(platforms.size()));

      // originalUrl은 unique 제약이 있으므로 UUID 기반으로 생성
      String originalUrl = "https://www." + randomPlatform.name().toLowerCase() + ".com/p/" + UUID.randomUUID() + "/";

      Content savedContent = contentRepository.save(
          Content.builder()
              .platform(randomPlatform)
              .status(ContentStatus.PENDING)
              .platformUploader(faker.name().fullName())
              .caption(faker.lorem().sentence(10, 20))
              .thumbnailUrl(faker.internet().image())
              .originalUrl(originalUrl)
              .title(faker.lorem().sentence(3, 5))
              .summary(faker.lorem().paragraph(3))
              .lastCheckedAt(LocalDateTime.now())
              .build()
      );

      savedContents.add(savedContent);
    }

    return TestResponse.builder()
        .contents(savedContents)
        .build();
  }
}
