package com.tripgether.sns.repository;

import com.tripgether.sns.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Content 엔티티에 대한 Repository
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {

}
