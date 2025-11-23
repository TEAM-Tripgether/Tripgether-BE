package com.tripgether.sns.repository;

import com.tripgether.sns.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {

  // SNS URL로 Content 조회
  Optional<Content> findByOriginalUrl(String originalUrl);

  // Member ID로 Content 목록 조회 (페이징)
  Page<Content> findByMemberId(UUID memberId, Pageable pageable);
}
