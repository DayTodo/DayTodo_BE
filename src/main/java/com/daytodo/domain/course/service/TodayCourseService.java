package com.daytodo.domain.course.service;

import com.daytodo.domain.course.converter.MemoryPhotoConverter;
import com.daytodo.domain.course.converter.TodayCourseConverter;
import com.daytodo.domain.course.dto.request.CourseReqDTO;
import com.daytodo.domain.course.dto.response.CourseResDTO;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.entity.MemoryPhoto;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.course.repository.CoursePlaceRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import com.daytodo.domain.course.repository.MemoryPhotoRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodayCourseService {

    private final CourseRepository courseRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final MemoryPhotoRepository memoryPhotoRepository;

    /*
     * 투데이 코스 조회
     * 오늘 진행 중인 코스가 없으면 예외가 아니라 todayCourse: null 로 응답한다.
     */
    @Transactional(readOnly = true)
    public CourseResDTO.GetTodayCourse getTodayCourse(Long userId) {
        Optional<Course> todayCourse = courseRepository.findMemberCoursesByDateAndStatus(
                userId,
                LocalDate.now(),
                CourseStatus.IN_PROGRESS,
                MemberStatus.JOINED
        ).stream().findFirst();

        if (todayCourse.isEmpty()) {
            return TodayCourseConverter.toTodayCourse(null, List.of(), List.of());
        }

        Course course = todayCourse.get();
        List<CourseMember> members =
                courseMemberRepository.findMembersByCourseId(course.getCourseId(), MemberStatus.JOINED);
        List<CoursePlace> places =
                coursePlaceRepository.findPlacesByCourseId(course.getCourseId());

        return TodayCourseConverter.toTodayCourse(course, members, places);
    }

    /*
     * 코스 종료
     * 진행 중(IN_PROGRESS)인 코스만 완료 처리할 수 있다.
     */
    @Transactional
    public CourseResDTO.CompleteCourse completeCourse(Long userId, Long courseId) {
        Course course = getCourseAsMember(userId, courseId);

        if (!course.isInProgress()) {
            throw new ProjectException(CourseErrorCode.INVALID_COURSE_STATUS);
        }

        course.complete();

        return TodayCourseConverter.toCompleteCourse(course);
    }

    /*
     * 추억 사진 저장
     * diary_id 는 비워둔 채 저장하고, 이후 해당 날짜의 일기가 작성될 때 연결한다.
     */
    @Transactional
    public CourseResDTO.SaveMemoryPhotos saveMemoryPhotos(
            Long userId,
            Long courseId,
            CourseReqDTO.SaveMemoryPhotos request
    ) {
        Course course = getCourseAsMember(userId, courseId);

        List<String> imageUrls = extractImageUrls(request);

        // 여러 사용자가 동시에 사진을 업로드해도 photo_order 가 겹치지 않도록,
        // 순번 계산~저장 구간을 코스 행 쓰기 락으로 직렬화한다.
        // (DB 유니크 제약(uk_memory_photo_course_id_photo_order)은 최종 방어선이다.)
        courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));
        int startOrder = memoryPhotoRepository.findMaxPhotoOrderByCourseId(courseId) + 1;

        List<MemoryPhoto> memoryPhotos = memoryPhotoRepository.saveAll(
                MemoryPhotoConverter.toMemoryPhotos(course, imageUrls, startOrder)
        );

        return MemoryPhotoConverter.toSaveMemoryPhotos(memoryPhotos);
    }

    // 공백 URL 은 걸러내고, 저장할 이미지가 하나도 없으면 400 으로 응답한다.
    private List<String> extractImageUrls(CourseReqDTO.SaveMemoryPhotos request) {
        if (request == null || request.imageUrls() == null) {
            throw new ProjectException(CourseErrorCode.EMPTY_MEMORY_PHOTO);
        }

        List<String> imageUrls = request.imageUrls().stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .toList();

        if (imageUrls.isEmpty()) {
            throw new ProjectException(CourseErrorCode.EMPTY_MEMORY_PHOTO);
        }

        return imageUrls;
    }

    private Course getCourseAsMember(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        // 코스 멤버가 아닌 경우도 코스 존재 여부를 노출하지 않기 위해 404 로 통일한다.
        boolean isMember = courseMemberRepository.existsByCourseCourseIdAndUserIdAndMemberStatus(
                courseId, userId, MemberStatus.JOINED
        );
        if (!isMember) {
            throw new ProjectException(CourseErrorCode.COURSE_NOT_FOUND);
        }

        return course;
    }
}
