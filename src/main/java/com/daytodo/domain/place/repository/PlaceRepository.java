package com.daytodo.domain.place.repository;

import com.daytodo.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 이미 존재한다면 이 파일은 만들지 않아도 됩니다 (findAllById는 JpaRepository 기본 제공 메서드).
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {
}