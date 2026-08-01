package com.daytodo.domain.place.repository;

import com.daytodo.domain.place.entity.Magazine;
import com.daytodo.domain.place.enums.MagazineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MagazineRepository extends JpaRepository<Magazine, Long> {

    /**
     * 목록 광고 오버레이용: 주어진 매거진 ID(=관광 contentId) 중 노출 상태인 것만 조회.
     * 결과의 magazineId 는 목록에서 isAd 표시·상단 정렬에 사용.
     */
    List<Magazine> findByMagazineIdInAndMagazineStatus(
            Collection<Long> magazineIds,
            MagazineStatus magazineStatus
    );
}
