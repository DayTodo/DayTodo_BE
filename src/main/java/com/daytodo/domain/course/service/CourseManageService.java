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
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.repository.RegionRepository;
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
    private final RegionRepository regionRepository;

    @Transactional
    public CourseResDto.SettingRes updateCourseSetting(Long courseId, Long userId, CourseReqDto.SettingReq request) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        // TODO: 인증/인가 붙일 때 - course.getOwner().getId().equals(userId) 등으로 검증 추가 필요 (Course 엔티티에 맞춰서)

        validateSameDayEditNotAllowed(course);          // PLN-001: 당일 코스 수정 불가
        validateDateChange(course, request.courseDate());
        validatePriceRange(request.minPrice(), request.maxPrice());

        boolean isPriceChanged = !course.getMinPrice().equals(request.minPrice())
                || !course.getMaxPrice().equals(request.maxPrice());

        boolean isRegionChanged = !course.getRegion().getRegionId().equals(request.regionId());

        course.setCourseName(request.courseName());

        if (isRegionChanged) {
            Region newRegion = regionRepository.findById(request.regionId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 지역을 찾을 수 없습니다.")); // TODO: 프로젝트 내 RegionErrorCode 등 적절한 예외 코드로 변경
            course.setRegion(newRegion);
        }

        course.setCourseDate(request.courseDate());
        course.setMinPrice(request.minPrice());
        course.setMaxPrice(request.maxPrice());
        course.setParticipantType(request.participantType());

        if (isPriceChanged || isRegionChanged) {
            course.resetRecommendationData(); // Course 엔티티에 추가하신 로직 실행
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

        CourseMember requester = (CourseMember) courseMemberRepository
                .findByCourseIdAndUserIdAndMemberStatus(courseId, userId, MemberStatus.JOINED)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED));

        if (requester.getMemberRole() != MemberRole.OWNER) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        CourseMember target = (CourseMember) courseMemberRepository
                .findByCourseIdAndUserIdAndMemberStatus(courseId, targetUserId, MemberStatus.JOINED)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_MEMBER_NOT_FOUND));

        if (target.getMemberRole() == MemberRole.OWNER) {
            throw new ProjectException(CourseErrorCode.OWNER_CANNOT_BE_REMOVED);
        }

        target.setMemberStatus(MemberStatus.LEFT);
    }
}