package com.tripgether.member.repository;

import com.tripgether.member.entity.Interest;
import com.tripgether.member.constant.InterestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

  List<Interest> findByCategory(InterestCategory category);

  @Query("SELECT i FROM Interest i ORDER BY i.category, i.name")
  List<Interest> findAllOrderByCategoryAndName();
}

