package com.daytodo.domain.course.converter;

import com.daytodo.domain.course.dto.response.CourseResDTO;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.MemoryPhoto;

import java.util.List;
import java.util.stream.IntStream;

/**
 * 이미지 URL <-> MemoryPhoto <-> 추억 사진 저장 응답 변환.
 */
public class MemoryPhotoConverter {

    private MemoryPhotoConverter() {
    }

    // startOrder 부터 순서대로 photoOrder 를 부여한다.
    public static List<MemoryPhoto> toMemoryPhotos(
            Course course,
            List<String> imageUrls,
            int startOrder
    ) {
        return IntStream.range(0, imageUrls.size())
                .mapToObj(index -> MemoryPhoto.builder()
                        .course(course)
                        .imageUrl(imageUrls.get(index))
                        .photoOrder(startOrder + index)
                        .build())
                .toList();
    }

    public static CourseResDTO.SaveMemoryPhotos toSaveMemoryPhotos(List<MemoryPhoto> memoryPhotos) {
        List<CourseResDTO.SaveMemoryPhotos.PhotoItem> photos = memoryPhotos.stream()
                .map(MemoryPhotoConverter::toPhotoItem)
                .toList();

        return new CourseResDTO.SaveMemoryPhotos(photos.size(), photos);
    }

    private static CourseResDTO.SaveMemoryPhotos.PhotoItem toPhotoItem(MemoryPhoto memoryPhoto) {
        return CourseResDTO.SaveMemoryPhotos.PhotoItem.builder()
                .memoryPhotoId(memoryPhoto.getId())
                .imageUrl(memoryPhoto.getImageUrl())
                .photoOrder(memoryPhoto.getPhotoOrder())
                .build();
    }
}
