package com.tripgether.member.repository;

import com.tripgether.member.entity.MemberInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberInterestRepository extends JpaRepository<MemberInterest, UUID> {

  List<MemberInterest> findByMemberId(UUID memberId);

  void deleteByMemberId(UUID memberId);
}

