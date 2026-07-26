package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.CourseReqDto;
import com.daytodo.domain.course.dto.CourseResDto;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.MemberRole;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import com.daytodo.domain.course.repository.CoursePlaceRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseManageService {

    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final CourseMemberRepository courseMemberRepository;

    @Transactional
    public CourseResDto.SettingRes updateCourseSetting(Long courseId, Long userId, CourseReqDto.SettingReq request) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        // TODO: 인증/인가 붙일 때 - course.getOwnerId().equals(userId) 검증 추가

        validateSameDayEditNotAllowed(course);          // PLN-001: 당일 코스 수정 불가
        validateDateChange(course, request.courseDate());
        validatePriceRange(request.minPrice(), request.maxPrice());

        // PLN-001: 가격대·지역 변경 시 추천 데이터 리셋 여부 판단 (값 변경 전에 비교해야 함)
        boolean isPriceChanged = !course.getMinPrice().equals(request.minPrice())
                || !course.getMaxPrice().equals(request.maxPrice());
        boolean isRegionChanged = !course.getRegionId().equals(request.regionId());

        course.setCourseName(request.courseName());
        course.setRegionId(request.regionId());
        course.setCourseDate(request.courseDate());
        course.setMinPrice(request.minPrice());
        course.setMaxPrice(request.maxPrice());
        course.setParticipantType(request.participantType());

        if (isPriceChanged || isRegionChanged) {
            course.resetRecommendationData(); // TODO: 실제 추천 데이터 리셋 로직으로 교체 (AI 연동 확정 후)
        }

        return CourseResDto.SettingRes.from(course);
    }

    private void validateSameDayEditNotAllowed(Course course) {
        if (course.getCourseDate().isEqual(LocalDate.now())) {
            throw new ProjectException(CourseErrorCode.COURSE_SAME_DAY_EDIT_NOT_ALLOWED);
        }
    }

    private void validateDateChange(Course course, LocalDate newDate) {
        boolean isInProgress = course.getCourseStatus() == CourseStatus.IN_PROGRESS;
        boolean isDateChanged = !course.getCourseDate().equals(newDate);

        if (isInProgress && isDateChanged) {
            throw new ProjectException(CourseErrorCode.COURSE_DATE_CHANGE_NOT_ALLOWED);
        }
    }

    private void validatePriceRange(Integer minPrice, Integer maxPrice) {
        if (minPrice > maxPrice) {
            throw new ProjectException(CourseErrorCode.INVALID_PRICE_RANGE);
        }
    }

    @Transactional(readOnly = true)
    public List<CourseResDto.CoursePlaceRes> getCoursePlaces(Long courseId, Long userId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        if (!courseMemberRepository.existsByCourseIdAndUserIdAndMemberStatus(
                courseId, userId, MemberStatus.JOINED)) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        List<CoursePlace> coursePlaces =
                coursePlaceRepository.findByCourseIdOrderByPlaceOrderAsc(courseId);

        // TODO: Place Repository를 전달받으면 placeId로 일괄 조회해 placeName을 매핑한다.
        return coursePlaces.stream()
                .map(CourseResDto.CoursePlaceRes::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResDto.CourseMemberRes> getCourseMembers(Long courseId, Long userId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        if (!courseMemberRepository.existsByCourseIdAndUserIdAndMemberStatus(
                courseId, userId, MemberStatus.JOINED)) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        List<CourseMember> courseMembers =
                courseMemberRepository.findByCourseIdAndMemberStatus(courseId, MemberStatus.JOINED);

        // TODO: User Repository를 전달받으면 userId로 일괄 조회해 nickname을 매핑한다.
        return courseMembers.stream()
                .map(CourseResDto.CourseMemberRes::from)
                .toList();
    }

    @Transactional
    public void kickCourseMember(Long courseId, Long targetUserId, Long userId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        CourseMember requester = courseMemberRepository
                .findByCourseIdAndUserIdAndMemberStatus(courseId, userId, MemberStatus.JOINED)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED));

        if (requester.getMemberRole() != MemberRole.OWNER) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        CourseMember target = courseMemberRepository
                .findByCourseIdAndUserIdAndMemberStatus(courseId, targetUserId, MemberStatus.JOINED)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_MEMBER_NOT_FOUND));

        if (target.getMemberRole() == MemberRole.OWNER) {
            throw new ProjectException(CourseErrorCode.OWNER_CANNOT_BE_REMOVED);
        }

        target.setMemberStatus(MemberStatus.LEFT);
    }
}