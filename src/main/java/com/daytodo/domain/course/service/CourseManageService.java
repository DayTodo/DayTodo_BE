package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.CourseReqDto;
import com.daytodo.domain.course.dto.CourseResDto;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.enums.CourseStatus;
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
    private final CourseMemberRepository courseMemberRepository; // 필드 추가

    @Transactional
    public CourseResDto.SettingRes updateCourseSetting(Long courseId, Long userId, CourseReqDto.SettingReq request) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        // TODO: 인증/인가 붙일 때 - course.getOwnerId().equals(userId) 검증 추가

        validateDateChange(course, request.courseDate());

        course.setCourseName(request.courseName());
        course.setRegionId(request.regionId());
        course.setCourseDate(request.courseDate());
        course.setMinPrice(request.minPrice());
        course.setMaxPrice(request.maxPrice());
        course.setParticipantType(request.participantType());

        return CourseResDto.SettingRes.from(course);
    }

    private void validateDateChange(Course course, LocalDate newDate) {
        boolean isInProgress = course.getCourseStatus() == CourseStatus.IN_PROGRESS;
        boolean isDateChanged = !course.getCourseDate().equals(newDate);

        if (isInProgress && isDateChanged) {
            throw new ProjectException(CourseErrorCode.COURSE_DATE_CHANGE_NOT_ALLOWED);
        }
    }

    @Transactional(readOnly = true)
    public List<CourseResDto.CoursePlaceRes> getCoursePlaces(Long courseId, Long userId) {

        // 1. 코스 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        // 2. 요청 사용자가 해당 코스의 멤버인지 확인
        if (!courseMemberRepository.existsByCourseIdAndUserIdAndMemberStatus(
                courseId, userId, MemberStatus.JOINED)) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        // 3. placeOrder 순으로 코스 장소 목록 조회
        List<CoursePlace> coursePlaces =
                coursePlaceRepository.findByCourseIdOrderByPlaceOrderAsc(courseId);

        // TODO: Place 도메인 완성되면 placeId로 Place 일괄 조회 후 placeName 매핑 추가
        return coursePlaces.stream()
                .map(CourseResDto.CoursePlaceRes::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResDto.CourseMemberRes> getCourseMembers(Long courseId, Long userId) {

        // 1. 코스 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        // 2. 요청 사용자가 해당 코스의 멤버인지 확인
        if (!courseMemberRepository.existsByCourseIdAndUserIdAndMemberStatus(
                courseId, userId, MemberStatus.JOINED)) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        // 3. 참여 중인(JOINED) 멤버 목록 조회
        List<CourseMember> courseMembers =
                courseMemberRepository.findByCourseIdAndMemberStatus(courseId, MemberStatus.JOINED);

        return courseMembers.stream()
                .map(CourseResDto.CourseMemberRes::from)
                .toList();
    }
}