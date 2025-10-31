package com.tripgether.sns.repository;

import com.tripgether.sns.entity.ContentPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * ContentPlace 연결 테이블에 대한 Repository
 */
@Repository
public interface ContentPlaceRepository extends JpaRepository<ContentPlace, UUID> {

}
