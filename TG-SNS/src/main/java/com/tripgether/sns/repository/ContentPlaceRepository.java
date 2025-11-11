package com.tripgether.sns.repository;

import com.tripgether.sns.entity.ContentPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContentPlaceRepository extends JpaRepository<ContentPlace, UUID> {

  // Content의 모든 ContentPlace 삭제
  void deleteByContentId(UUID contentId);
}
