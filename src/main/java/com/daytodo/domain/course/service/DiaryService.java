package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.DiaryRequest;
import com.daytodo.domain.course.dto.DiaryResponse;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.entity.Diary;
import com.daytodo.domain.course.entity.MemoryPhoto;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.exception.code.DiaryErrorCode;
import com.daytodo.domain.course.repository.CoursePlaceRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import com.daytodo.domain.course.repository.DiaryRepository;
import com.daytodo.domain.course.repository.MemoryPhotoRepository;
import com.daytodo.domain.place.entity.Place;
import com.daytodo.domain.place.repository.PlaceRepository;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemoryPhotoRepository memoryPhotoRepository;
    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    @Transactional
    public DiaryResponse.Write writeDiary(Long userId, DiaryRequest.Write request) {
        User user = getActiveUser(userId);
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        if (course.getCourseStatus() != CourseStatus.COMPLETED) {
            throw new ProjectException(DiaryErrorCode.COURSE_NOT_COMPLETED);
        }

        Diary diary = diaryRepository.findByUserIdAndCourse_CourseId(userId, request.courseId())
                .map(existing -> {
                    existing.writeContent(request.content());
                    return existing;
                })
                .orElseGet(() -> diaryRepository.save(Diary.builder()
                        .user(user)
                        .course(course)
                        .diaryDate(course.getCourseDate())
                        .content(request.content())
                        .build()));

        return new DiaryResponse.Write(diary.getId(), course.getCourseId(), diary.getDiaryDate(), diary.getContent());
    }

    public DiaryResponse.Calendar getCalendar(Long userId, Integer year, Integer month) {
        getActiveUser(userId);
        YearMonth yearMonth = toYearMonth(year, month);
        List<Diary> diaries = diaryRepository.findAllByUserIdAndDiaryDateBetween(
                userId, yearMonth.atDay(1), yearMonth.atEndOfMonth()
        );
        List<DiaryResponse.CalendarDate> dates = diaries.stream()
                .map(diary -> new DiaryResponse.CalendarDate(
                        diary.getDiaryDate(),
                        diary.getId(),
                        diary.getContent() != null && !diary.getContent().isBlank()
                ))
                .toList();
        return new DiaryResponse.Calendar(year, month, dates);
    }

    public DiaryResponse.Photos getPhotosByCourse(Long userId, Long courseId) {
        getActiveUser(userId);
        List<MemoryPhoto> photos = memoryPhotoRepository
                .findAllByDiary_Course_CourseIdAndDiary_User_IdOrderByPhotoOrderAsc(courseId, userId);
        return new DiaryResponse.Photos(courseId, toPhotoResponses(photos));
    }

    public DiaryResponse.MemoryByDate getMemoryByDate(Long userId, LocalDate date) {
        getActiveUser(userId);
        // 같은 날짜에 완료된 코스가 여러 개면 일기가 여러 건 생길 수 있어 List로 조회하고,
        // 그중 가장 최근에 생성된 일기를 대표로 반환한다 (단건 Optional 조회 시 500 방지).
        List<Diary> diaries = diaryRepository.findAllByUserIdAndDiaryDate(userId, date);
        Diary diary = diaries.stream()
                .max(Comparator.comparing(Diary::getCreatedAt))
                .orElseThrow(() -> new ProjectException(DiaryErrorCode.DIARY_NOT_FOUND));
        List<MemoryPhoto> photos = memoryPhotoRepository.findAllByDiary_IdOrderByPhotoOrderAsc(diary.getId());

        return new DiaryResponse.MemoryByDate(
                diary.getId(),
                diary.getCourse().getCourseId(),
                diary.getCourse().getCourseName(),
                diary.getDiaryDate(),
                diary.getContent(),
                toPhotoResponses(photos)
        );
    }

    public DiaryResponse.VisitedCourse getVisitedCourse(Long userId, Long diaryId) {
        getActiveUser(userId);
        Diary diary = diaryRepository.findById(diaryId)
                .filter(found -> found.getUser().getId().equals(userId))
                .orElseThrow(() -> new ProjectException(DiaryErrorCode.DIARY_NOT_FOUND));

        Course course = diary.getCourse();
        List<CoursePlace> coursePlaces = coursePlaceRepository
                .findAllByCourse_CourseIdOrderByPlaceOrderAsc(course.getCourseId());

        List<Long> placeIds = coursePlaces.stream()
                .map(cp -> cp.getPlace().getPlaceId())
                .toList();
        Map<Long, Place> placesById = placeRepository.findAllById(placeIds).stream()
                .collect(Collectors.toMap(Place::getPlaceId, Function.identity()));

        List<DiaryResponse.CoursePlaceInfo> places = coursePlaces.stream()
                .map(coursePlace -> {
                    Place place = placesById.get(coursePlace.getPlace().getPlaceId());
                    return new DiaryResponse.CoursePlaceInfo(
                            place.getPlaceId(),
                            place.getPlaceName(),
                            place.getCategory(),
                            place.getAddress(),
                            place.getPhone(),
                            place.getDescription(),
                            place.getImageUrl(),
                            coursePlace.getVisitedAt()
                    );
                })
                .toList();

        return new DiaryResponse.VisitedCourse(
                course.getCourseId(),
                course.getCourseName(),
                course.getCourseDate(),
                places
        );
    }

    private List<DiaryResponse.Photo> toPhotoResponses(List<MemoryPhoto> photos) {
        return photos.stream()
                .map(photo -> new DiaryResponse.Photo(photo.getId(), photo.getImageUrl(), photo.getPhotoOrder()))
                .toList();
    }

    private User getActiveUser(Long userId) {
        return userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ProjectException(DiaryErrorCode.USER_NOT_FOUND));
    }

    private YearMonth toYearMonth(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new ProjectException(DiaryErrorCode.INVALID_CALENDAR_DATE);
        }
        try {
            return YearMonth.of(year, month);
        } catch (RuntimeException exception) {
            throw new ProjectException(DiaryErrorCode.INVALID_CALENDAR_DATE);
        }
    }
}