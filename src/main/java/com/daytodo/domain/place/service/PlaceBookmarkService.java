package com.daytodo.domain.place.service;

import com.daytodo.domain.place.converter.BookmarkPlaceConverter;
import com.daytodo.domain.place.dto.request.PlaceReqDTO;
import com.daytodo.domain.place.dto.response.PlaceResDTO;
import com.daytodo.domain.place.entity.mapping.BookmarkPlace;
import com.daytodo.domain.place.enums.BookmarkSortType;
import com.daytodo.domain.place.repository.BookmarkPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceBookmarkService {

    private final BookmarkPlaceRepository bookmarkPlaceRepository;

    @Transactional(readOnly = true)
    public PlaceResDTO.GetBookmarkList getBookmarkList(
            Long userId,
            PlaceReqDTO.GetBookmarkList request
    ) {
        BookmarkSortType sort = (request.sort() == null) ? BookmarkSortType.RECENT : request.sort();
        Long regionId = request.regionId();

        List<BookmarkPlace> bookmarkPlaces = switch (sort) {
            case RECENT -> bookmarkPlaceRepository.findAllOrderByRecent(userId, regionId);
            case OLDEST -> bookmarkPlaceRepository.findAllOrderByOldest(userId, regionId);
            case NAME -> bookmarkPlaceRepository.findAllOrderByPlaceName(userId, regionId);
            case POPULAR -> bookmarkPlaceRepository.findAllOrderByPopular(userId, regionId);
        };

        return BookmarkPlaceConverter.toBookmarkList(bookmarkPlaces);
    }
}
