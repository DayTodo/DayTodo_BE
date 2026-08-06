package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.DiaryRequest;
import com.daytodo.domain.course.dto.DiaryResponse;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.entity.Diary;
import com.daytodo.domain.course.entity.MemoryPhoto;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.exception.code.DiaryErrorCode;
import com.daytodo.domain.course.repository.CourseMemberRepository;
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
    private final CourseMemberRepository courseMemberRepository;

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
        // 추억 사진은 코스 멤버 전원이 함께 보는 공용 사진이라(피그마 '기록' 화면에서
        // 여러 멤버가 같은 사진에 메모를 남기는 것으로 확인), 요청자가 그 코스의 멤버인지만
        // 확인하고 course 단위로 사진을 조회한다. 특정 멤버의 diary에 연결됐는지는 보지 않는다.
        requireCourseMember(userId, courseId);
        List<MemoryPhoto> photos = memoryPhotoRepository
                .findAllByCourse_CourseIdOrderByPhotoOrderAsc(courseId);
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

        // diary는 유저가 나중에 코스를 나가도(LEFT) 그대로 남아있으므로, 공용 사진을
        // 반환하기 전에 지금도 그 코스의 JOINED 멤버인지 다시 확인한다.
        requireCourseMember(userId, diary.getCourse().getCourseId());

        // 사진은 diary가 아닌 course 공용이므로, 이 diary가 속한 코스 기준으로 조회한다.
        List<MemoryPhoto> photos = memoryPhotoRepository
                .findAllByCourse_CourseIdOrderByPhotoOrderAsc(diary.getCourse().getCourseId());

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

    private void requireCourseMember(Long userId, Long courseId) {
        boolean isMember = courseMemberRepository
                .existsByCourseCourseIdAndUserIdAndMemberStatus(courseId, userId, MemberStatus.JOINED);
        if (!isMember) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }
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