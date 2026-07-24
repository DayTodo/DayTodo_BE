package com.daytodo.domain.course.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class DiaryResponse {

    private DiaryResponse() {
    }

    public record Write(Long diaryId, Long courseId, LocalDate diaryDate, String content) {
    }

    public record CalendarDate(LocalDate date, Long diaryId, boolean hasContent) {
    }

    public record Calendar(int year, int month, List<CalendarDate> dates) {
        public Calendar {
            dates = List.copyOf(dates);
        }
    }

    public record Photo(Long memoryPhotoId, String imageUrl, int photoOrder) {
    }

    public record Photos(Long courseId, List<Photo> photos) {
        public Photos {
            photos = List.copyOf(photos);
        }
    }

    public record CoursePlaceInfo(
            Long placeId,
            String placeName,
            String category,
            String address,
            String phone,
            String description,
            String imageUrl,
            LocalDateTime visitedAt
    ) {
    }

    public record VisitedCourse(
            Long courseId,
            String courseName,
            LocalDate courseDate,
            List<CoursePlaceInfo> places
    ) {
        public VisitedCourse {
            places = List.copyOf(places);
        }
    }

    public record MemoryByDate(
            Long diaryId,
            Long courseId,
            String courseName,
            LocalDate diaryDate,
            String content,
            List<Photo> photos
    ) {
        public MemoryByDate {
            photos = List.copyOf(photos);
        }
    }
}