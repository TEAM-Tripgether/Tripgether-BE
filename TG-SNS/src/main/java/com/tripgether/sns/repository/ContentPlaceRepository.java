package com.tripgether.sns.repository;

import com.tripgether.sns.entity.ContentPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContentPlaceRepository extends JpaRepository<ContentPlace, UUID> {

  // Content의 모든 ContentPlace 삭제
  void deleteByContentId(UUID contentId);

  // Content의 모든 ContentPlace 삭제 후 즉시 flush
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("DELETE FROM ContentPlace cp WHERE cp.content.id = :contentId")
  void deleteByContentIdWithFlush(@Param("contentId") UUID contentId);
}
