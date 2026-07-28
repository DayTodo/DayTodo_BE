package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.CourseRequest;
import com.daytodo.domain.course.dto.CourseResponse;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.CoursePlace;
import com.daytodo.domain.course.entity.InviteCode;
import com.daytodo.domain.course.enums.CourseStatus;
import com.daytodo.domain.course.enums.HomeBannerStatus;
import com.daytodo.domain.course.enums.MemberRole;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.course.repository.CoursePlaceRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import com.daytodo.domain.course.repository.InviteCodeRepository;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
import com.daytodo.domain.region.exception.code.RegionErrorCode;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.service.UserService;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private static final int UPCOMING_LIMIT = 4;
    private static final int UPCOMING_BANNER_DAYS = 3;
    private static final int INVITE_CODE_LENGTH = 12;
    private static final int INVITE_CODE_GENERATION_ATTEMPTS = 5;

    private final CourseRepository courseRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final RegionRepository regionRepository;
    private final UserService userService;
    private final Clock clock;

    public CourseResponse.Courses getCourses(Long userId, LocalDate startDate, LocalDate endDate) {
        userService.getActiveUser(userId);
        validatePeriod(startDate, endDate);
        LocalDate today = LocalDate.now(clock);

        List<Course> inProgress = courseRepository.findMemberCoursesByStatus(
                userId, MemberStatus.JOINED, CourseStatus.IN_PROGRESS
        );
        List<Course> upcoming = courseRepository.findUpcomingMemberCourses(
                userId,
                MemberStatus.JOINED,
                CourseStatus.PLANNING,
                today,
                PageRequest.of(0, UPCOMING_LIMIT)
        );
        List<Course> created = findCreatedCourses(userId, startDate, endDate);

        List<Long> allIds = java.util.stream.Stream.of(inProgress, upcoming, created)
                .flatMap(List::stream)
                .map(Course::getCourseId)
                .distinct()
                .toList();
        Map<Long, Long> memberCounts = memberCounts(allIds);
        Map<Long, Long> placeCounts = placeCounts(created.stream().map(Course::getCourseId).toList());

        return new CourseResponse.Courses(
                createBanner(userId, today, inProgress),
                inProgress.stream().map(course -> toCard(course, today, memberCounts)).toList(),
                upcoming.stream().map(course -> toCard(course, today, memberCounts)).toList(),
                created.stream().map(course -> new CourseResponse.CreatedCourse(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getCourseDate(),
                        memberCounts.getOrDefault(course.getCourseId(), 0L),
                        placeCounts.getOrDefault(course.getCourseId(), 0L),
                        course.getParticipantType()
                )).toList()
        );
    }

    public CourseResponse.Calendar getCalendar(Long userId, Integer year, Integer month) {
        userService.getActiveUser(userId);
        YearMonth yearMonth = toYearMonth(year, month);
        List<Course> courses = courseRepository.findCalendarCourses(
                userId,
                MemberStatus.JOINED,
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );
        Map<Long, Long> memberCounts = memberCounts(courses.stream().map(Course::getCourseId).toList());

        Map<LocalDate, List<Course>> grouped = courses.stream().collect(
                java.util.stream.Collectors.groupingBy(
                        Course::getCourseDate,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                )
        );
        List<CourseResponse.DateSchedule> schedules = grouped.entrySet().stream()
                .map(entry -> new CourseResponse.DateSchedule(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(course -> new CourseResponse.CalendarCourse(
                                        course.getCourseId(),
                                        course.getCourseName(),
                                        course.getParticipantType(),
                                        memberCounts.getOrDefault(course.getCourseId(), 0L)
                                ))
                                .toList()
                ))
                .toList();
        return new CourseResponse.Calendar(year, month, schedules);
    }

    @Transactional
    public CourseResponse.Created createCourse(Long userId, CourseRequest.Create request) {
        User owner = userService.getActiveUser(userId);
        LocalDate today = LocalDate.now(clock);
        if (request.courseDate().isBefore(today)) {
            throw new ProjectException(CourseErrorCode.PAST_COURSE_DATE);
        }
        if (request.maxPrice() < request.minPrice()) {
            throw new ProjectException(CourseErrorCode.INVALID_PRICE_RANGE);
        }

        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new ProjectException(RegionErrorCode.REGION_NOT_FOUND));
        validateSeoulRegion(region);

        Course course = courseRepository.save(new Course(
                owner,
                region,
                request.courseName().trim(),
                request.courseDate(),
                request.minPrice(),
                request.maxPrice(),
                request.participantType()
        ));
        courseMemberRepository.save(new CourseMember(
                course, owner, MemberRole.OWNER, MemberStatus.JOINED
        ));

        String code = generateInviteCode();
        LocalDateTime expiredAt = request.courseDate().atTime(LocalTime.MAX);
        inviteCodeRepository.save(new InviteCode(course, owner, code, expiredAt));
        return new CourseResponse.Created(course.getCourseId(), code, expiredAt);
    }

    @Transactional
    public CourseResponse.Joined joinCourse(Long userId, CourseRequest.Join request) {
        User user = userService.getActiveUser(userId);
        InviteCode inviteCode = inviteCodeRepository.findByCode(request.inviteCode().trim())
                .orElseThrow(() -> new ProjectException(CourseErrorCode.INVALID_INVITE_CODE));
        if (!Boolean.TRUE.equals(inviteCode.getIsActive())) {
            throw new ProjectException(CourseErrorCode.INVALID_INVITE_CODE);
        }
        if (inviteCode.getExpiredAt().isBefore(LocalDateTime.now(clock))) {
            throw new ProjectException(CourseErrorCode.EXPIRED_INVITE_CODE);
        }

        Course course = inviteCode.getCourse();
        if (course.getCourseStatus() != CourseStatus.PLANNING
                && course.getCourseStatus() != CourseStatus.IN_PROGRESS) {
            throw new ProjectException(CourseErrorCode.COURSE_NOT_JOINABLE);
        }

        CourseMember existing = courseMemberRepository
                .findByCourseCourseIdAndUserId(course.getCourseId(), userId)
                .orElse(null);
        if (existing != null) {
            if (existing.getMemberStatus() == MemberStatus.JOINED) {
                throw new ProjectException(CourseErrorCode.ALREADY_COURSE_MEMBER);
            }
            existing.join();
            return new CourseResponse.Joined(course.getCourseId(), course.getCourseName());
        }

        try {
            courseMemberRepository.saveAndFlush(new CourseMember(
                    course, user, MemberRole.MEMBER, MemberStatus.JOINED
            ));
        } catch (DataIntegrityViolationException exception) {
            throw new ProjectException(CourseErrorCode.ALREADY_COURSE_MEMBER);
        }
        return new CourseResponse.Joined(course.getCourseId(), course.getCourseName());
    }

    private List<Course> findCreatedCourses(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return courseRepository.findAllByOwnerIdAndCourseDateBetweenOrderByCreatedAtDesc(
                    userId, startDate, endDate
            );
        }
        if (startDate != null) {
            return courseRepository.findAllByOwnerIdAndCourseDateGreaterThanEqualOrderByCreatedAtDesc(
                    userId, startDate
            );
        }
        if (endDate != null) {
            return courseRepository.findAllByOwnerIdAndCourseDateLessThanEqualOrderByCreatedAtDesc(
                    userId, endDate
            );
        }
        return courseRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId);
    }

    private CourseResponse.Banner createBanner(Long userId, LocalDate today, List<Course> inProgress) {
        if (!inProgress.isEmpty()) {
            Course course = inProgress.get(0);
            return new CourseResponse.Banner(
                    HomeBannerStatus.IN_PROGRESS,
                    "오늘 " + course.getCourseName() + " 일정이 있어요!",
                    course.getCourseId()
            );
        }
        List<Course> approaching = courseRepository.findApproachingMemberCourses(
                userId,
                MemberStatus.JOINED,
                CourseStatus.PLANNING,
                today,
                today.plusDays(UPCOMING_BANNER_DAYS)
        );
        if (!approaching.isEmpty()) {
            return new CourseResponse.Banner(
                    HomeBannerStatus.UPCOMING,
                    "곧 다가오는 일정이 있어요",
                    approaching.get(0).getCourseId()
            );
        }
        return new CourseResponse.Banner(
                HomeBannerStatus.EMPTY,
                "오늘은 아무 일정이 없어요",
                null
        );
    }

    private CourseResponse.CourseCard toCard(
            Course course,
            LocalDate today,
            Map<Long, Long> memberCounts
    ) {
        return new CourseResponse.CourseCard(
                course.getCourseId(),
                course.getCourseName(),
                course.getCourseDate(),
                ChronoUnit.DAYS.between(today, course.getCourseDate()),
                memberCounts.getOrDefault(course.getCourseId(), 0L),
                course.getParticipantType()
        );
    }

    private Map<Long, Long> memberCounts(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> result = new HashMap<>();
        courseMemberRepository.countMembersByCourseIds(courseIds, MemberStatus.JOINED)
                .forEach(count -> result.put(count.getCourseId(), count.getCount()));
        return result;
    }

    private Map<Long, Long> placeCounts(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> result = new HashMap<>();
        coursePlaceRepository.countPlacesByCourseIds(courseIds)
                .forEach(count -> result.put(count.getCourseId(), count.getCount()));
        return result;
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ProjectException(CourseErrorCode.INVALID_COURSE_PERIOD);
        }
    }

    private YearMonth toYearMonth(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new ProjectException(CourseErrorCode.INVALID_CALENDAR_DATE);
        }
        try {
            return YearMonth.of(year, month);
        } catch (RuntimeException exception) {
            throw new ProjectException(CourseErrorCode.INVALID_CALENDAR_DATE);
        }
    }

    private void validateSeoulRegion(Region region) {
        String parentName = region.getParent() == null ? null : region.getParent().getRegionName();
        boolean seoul = "서울".equals(parentName) || "서울특별시".equals(parentName);
        if (region.getRegionLevel() != RegionLevel.SIGUNGU || !seoul) {
            throw new ProjectException(RegionErrorCode.UNSUPPORTED_REGION);
        }
    }

    private String generateInviteCode() {
        for (int attempt = 0; attempt < INVITE_CODE_GENERATION_ATTEMPTS; attempt++) {
            String code = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, INVITE_CODE_LENGTH)
                    .toUpperCase(Locale.ROOT);
            if (!inviteCodeRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new ProjectException(CourseErrorCode.INVITE_CODE_GENERATION_FAILED);
    }

    @Transactional
    public CourseResponse.Setting updateCourseSetting(Long courseId, Long userId, CourseRequest.Setting request) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        CourseMember requester = courseMemberRepository
                .findByCourseCourseIdAndUserIdAndMemberStatus(courseId, userId, MemberStatus.JOINED)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED));

        if (requester.getMemberRole() != MemberRole.OWNER) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        validateSameDayEditNotAllowed(course);          // PLN-001: 당일 코스 수정 불가
        validateDateChange(course, request.courseDate());
        validatePriceRange(request.minPrice(), request.maxPrice());

        // PLN-001: 가격대·지역 변경 시 추천 데이터 리셋 여부 판단 (값 변경 전에 비교해야 함)
        boolean isPriceChanged = !course.getMinPrice().equals(request.minPrice())
                || !course.getMaxPrice().equals(request.maxPrice());
        boolean isRegionChanged = !course.getRegion().getRegionId().equals(request.regionId());

        course.setCourseName(request.courseName());

        // 지역이 변경된 경우에만 Region 객체를 조회하여 업데이트
        if (isRegionChanged) {
            Region region = regionRepository.findById(request.regionId())
                    .orElseThrow(() -> new ProjectException(RegionErrorCode.REGION_NOT_FOUND));
            course.setRegion(region);
        }

        course.setCourseDate(request.courseDate());
        course.setMinPrice(request.minPrice());
        course.setMaxPrice(request.maxPrice());
        course.setParticipantType(request.participantType());

        if (isPriceChanged || isRegionChanged) {
            // TODO: 실제 추천 데이터 리셋 로직으로 교체 (AI 연동 확정 후)
        }

        return CourseResponse.Setting.from(course);
    }

    private void validateSameDayEditNotAllowed(Course course) {
        if (course.getCourseDate().isEqual(LocalDate.now(clock))) { // 상단의 clock 활용
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
    public List<CourseResponse.CoursePlace> getCoursePlaces(Long courseId, Long userId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        if (!courseMemberRepository.existsByCourseCourseIdAndUserIdAndMemberStatus(
                courseId, userId, MemberStatus.JOINED)) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        List<CoursePlace> coursePlaces =
                coursePlaceRepository.findPlacesByCourseId(courseId);   // ← 변경

        return coursePlaces.stream()
                .map(CourseResponse.CoursePlace::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse.CourseMember> getCourseMembers(Long courseId, Long userId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        if (!courseMemberRepository.existsByCourseCourseIdAndUserIdAndMemberStatus(
                courseId, userId, MemberStatus.JOINED)) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        List<CourseMember> courseMembers =
                courseMemberRepository.findMembersByCourseId(courseId, MemberStatus.JOINED);   // ← 변경

        return courseMembers.stream()
                .map(CourseResponse.CourseMember::from)
                .toList();
    }

    @Transactional
    public void kickCourseMember(Long courseId, Long targetUserId, Long userId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));

        CourseMember requester = courseMemberRepository
                .findByCourseCourseIdAndUserIdAndMemberStatus(courseId, userId, MemberStatus.JOINED)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED));

        if (requester.getMemberRole() != MemberRole.OWNER) {
            throw new ProjectException(CourseErrorCode.COURSE_ACCESS_DENIED);
        }

        CourseMember target = courseMemberRepository
                .findByCourseCourseIdAndUserIdAndMemberStatus(courseId, targetUserId, MemberStatus.JOINED)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_MEMBER_NOT_FOUND));

        if (target.getMemberRole() == MemberRole.OWNER) {
            throw new ProjectException(CourseErrorCode.OWNER_CANNOT_BE_REMOVED);
        }

        target.setMemberStatus(MemberStatus.LEFT);
    }
}
