package com.tripgether.sns.repository;

import com.tripgether.member.entity.Member;
import com.tripgether.sns.entity.ContentPlace;
import java.util.List;
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

  // member가 소유한 content 들과 연결된 ContentPlace 중 최신순 10개
  List<ContentPlace> findTop10ByContent_MemberOrderByCreatedAtDesc(Member member);

  // memberId로 바로 쓰고 싶으면 아래처럼도 가능
  // List<ContentPlace> findTop10ByContent_Member_IdOrderByCreatedAtDesc(UUID memberId);
}
