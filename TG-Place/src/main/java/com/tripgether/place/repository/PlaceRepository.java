package com.tripgether.place.repository;

import com.tripgether.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Place 엔티티에 대한 Repository
 */
@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {

  /**
   * 이름과 좌표로 장소 조회 (중복 방지용)
   *
   * @param name      장소명
   * @param latitude  위도
   * @param longitude 경도
   * @return Optional<Place>
   */
  Optional<Place> findByNameAndLatitudeAndLongitude(String name, BigDecimal latitude, BigDecimal longitude);
}
