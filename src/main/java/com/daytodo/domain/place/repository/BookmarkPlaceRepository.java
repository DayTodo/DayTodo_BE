package com.daytodo.domain.place.repository;

import com.daytodo.domain.place.entity.mapping.BookmarkPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkPlaceRepository extends JpaRepository<BookmarkPlace, Long> {

    /*
     * 저장 목록 공통 조회 조건
     * - 응답의 regionName 조립에 Region, Region.parent 가 필요하므로 함께 fetch
     * - regionId 가 시/도이면 하위 구(Region.parent)까지 포함
     * - Place.region 은 nullable 이므로 left join 으로 둠
     */
    String BOOKMARK_LIST_QUERY = """
            select bp
            from BookmarkPlace bp
            join fetch bp.place p
            left join fetch p.region r
            left join fetch r.parent rp
            where bp.user.id = :userId
              and (:regionId is null or r.regionId = :regionId or rp.regionId = :regionId)
            """;

    @Query(BOOKMARK_LIST_QUERY + " order by bp.createdAt desc")
    List<BookmarkPlace> findAllOrderByRecent(
            @Param("userId") Long userId,
            @Param("regionId") Long regionId
    );

    @Query(BOOKMARK_LIST_QUERY + " order by bp.createdAt asc")
    List<BookmarkPlace> findAllOrderByOldest(
            @Param("userId") Long userId,
            @Param("regionId") Long regionId
    );

    @Query(BOOKMARK_LIST_QUERY + " order by p.placeName asc")
    List<BookmarkPlace> findAllOrderByPlaceName(
            @Param("userId") Long userId,
            @Param("regionId") Long regionId
    );

    /*
     * 인기순: 전체 유저 기준 해당 Place 의 북마크 수를 집계하여 정렬
     * 정렬을 DB에 맡겨야 이후 페이지네이션을 붙여도 순서가 유지
     */
    @Query(BOOKMARK_LIST_QUERY + """
             order by (select count(other) from BookmarkPlace other where other.place = p) desc,
                      bp.createdAt desc
            """)
    List<BookmarkPlace> findAllOrderByPopular(
            @Param("userId") Long userId,
            @Param("regionId") Long regionId
    );
}
