package com.daytodo.domain.course.service;

import com.daytodo.domain.course.converter.MemoryPhotoConverter;
import com.daytodo.domain.course.converter.TodayCourseConverter;
import com.daytodo.domain.course.dto.request.TodayCourseRequest;
import com.daytodo.domain.course.dto.response.TodayCourseResponse;
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
import com.daytodo.domain.course.storage.MemoryPhotoStorage;
import com.daytodo.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodayCourseService {

    private final CourseRepository courseRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final MemoryPhotoRepository memoryPhotoRepository;
    private final MemoryPhotoStorage memoryPhotoStorage;
    private final TodayCoursePromoter todayCoursePromoter;
    private final Clock clock;

    /*
     * 투데이 코스 조회
     * 오늘 날짜의 코스가 없으면 예외가 아니라 todayCourse: null 로 응답한다.
     * 별도의 상태 전이 스케줄러가 없으므로, 조회 진입 시점에 오늘 날짜의 PLANNING
     * 코스를 IN_PROGRESS 로 승격시킨다(홈/투데이 공용 규칙).
     */
    @Transactional
    public TodayCourseResponse.GetTodayCourse getTodayCourse(Long userId) {
        LocalDate today = LocalDate.now(clock);
        todayCoursePromoter.promoteDueCourses(userId, today);

        Optional<Course> todayCourse = courseRepository.findMemberCoursesByDateAndStatuses(
                userId,
                today,
                List.of(CourseStatus.IN_PROGRESS),
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
    public TodayCourseResponse.CompleteCourse completeCourse(Long userId, Long courseId) {
        Course course = getCourseAsMember(userId, courseId);

        if (!course.isInProgress()) {
            throw new ProjectException(CourseErrorCode.INVALID_COURSE_STATUS);
        }

        course.complete();

        return TodayCourseConverter.toCompleteCourse(course);
    }

    /*
     * 추억 사진 저장
     * 클라이언트가 보낸 이미지 파일을 S3 에 올려 영구 URL 로 치환한 뒤 기록한다.
     * (예전엔 기기 로컬 content:// URI 를 그대로 저장해 시간이 지나면 못 읽고 회색으로 깨졌다.)
     * diary_id 는 비워둔 채 저장하고, 이후 해당 날짜의 일기가 작성될 때 연결한다.
     */
    @Transactional
    public TodayCourseResponse.SaveMemoryPhotos saveMemoryPhotos(
            Long userId,
            Long courseId,
            List<MultipartFile> images
    ) {
        Course course = getCourseAsMember(userId, courseId);

        List<MultipartFile> validImages = extractImages(images);

        // 업로드한 S3 객체 URL. 뒤이은 DB 저장이 실패해 트랜잭션이 롤백되면 정리한다(고아 객체 방지).
        List<String> uploadedUrls = new ArrayList<>();
        registerPhotoCleanupOnRollback(uploadedUrls);
        for (MultipartFile image : validImages) {
            uploadedUrls.add(memoryPhotoStorage.upload(courseId, image));
        }

        // 여러 사용자가 동시에 사진을 업로드해도 photo_order 가 겹치지 않도록,
        // 순번 계산~저장 구간을 코스 행 쓰기 락으로 직렬화한다.
        // (DB 유니크 제약(uk_memory_photo_course_id_photo_order)은 최종 방어선이다.)
        courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_NOT_FOUND));
        int startOrder = memoryPhotoRepository.findMaxPhotoOrderByCourseId(courseId) + 1;

        List<MemoryPhoto> memoryPhotos = memoryPhotoRepository.saveAll(
                MemoryPhotoConverter.toMemoryPhotos(course, uploadedUrls, startOrder)
        );

        return MemoryPhotoConverter.toSaveMemoryPhotos(memoryPhotos);
    }

    // 비어있는 파트는 걸러내고, 업로드할 이미지가 하나도 없으면 400 으로 응답한다.
    private List<MultipartFile> extractImages(List<MultipartFile> images) {
        if (images == null) {
            throw new ProjectException(CourseErrorCode.EMPTY_MEMORY_PHOTO);
        }

        List<MultipartFile> validImages = images.stream()
                .filter(image -> image != null && !image.isEmpty())
                .toList();

        if (validImages.isEmpty()) {
            throw new ProjectException(CourseErrorCode.EMPTY_MEMORY_PHOTO);
        }

        return validImages;
    }

    // DB 저장 실패로 트랜잭션이 롤백되면 이미 업로드한 S3 객체를 삭제한다.
    // (uploadedUrls 는 업로드가 진행되며 채워지고, afterCompletion 은 트랜잭션 종료 후 실행되므로 최종 목록을 본다.)
    private void registerPhotoCleanupOnRollback(List<String> uploadedUrls) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    uploadedUrls.forEach(TodayCourseService.this::deleteQuietly);
                }
            }
        });
    }

    private void deleteQuietly(String imageUrl) {
        try {
            memoryPhotoStorage.deleteByUrl(imageUrl);
        } catch (RuntimeException exception) {
            log.warn("추억 사진 S3 삭제 실패. url={}", imageUrl, exception);
        }
    }

    /*
     * 코스 장소 삭제
     * coursePlaceId 로 코스에서 장소를 제거하고, 남은 목록을 순서대로 반환한다.
     * placeOrder 는 재정렬하지 않는다(조회는 order asc, 추가는 max+1 이라 빈 순번이 있어도 무방).
     */
    @Transactional
    public TodayCourseResponse.GetCoursePlaces removePlaceFromCourse(
            Long userId,
            Long courseId,
            Long coursePlaceId
    ) {
        getCourseAsMember(userId, courseId);

        CoursePlace coursePlace = coursePlaceRepository.findById(coursePlaceId)
                .orElseThrow(() -> new ProjectException(CourseErrorCode.COURSE_PLACE_NOT_FOUND));

        // 다른 코스의 장소를 삭제하지 못하도록 소속 코스를 확인한다.
        if (!coursePlace.getCourse().getCourseId().equals(courseId)) {
            throw new ProjectException(CourseErrorCode.COURSE_PLACE_NOT_FOUND);
        }

        coursePlaceRepository.delete(coursePlace);

        return TodayCourseConverter.toCoursePlaces(coursePlaceRepository.findPlacesByCourseId(courseId));
    }

    /*
     * 코스 장소 순서 변경
     * 전달받은 순서대로 placeOrder 를 갱신하고, 변경된 목록을 순서대로 반환한다.
     * 순서 충돌을 막기 위해 코스의 전체 장소를 빠짐없이 재배열하는 경우만 허용한다.
     */
    @Transactional
    public TodayCourseResponse.GetCoursePlaces reorderCoursePlaces(
            Long userId,
            Long courseId,
            TodayCourseRequest.ReorderCoursePlaces request
    ) {
        List<Long> orderedIds = (request == null) ? null : request.orderedCoursePlaceIds();
        if (orderedIds == null || orderedIds.isEmpty()
                || orderedIds.stream().anyMatch(Objects::isNull)
                || orderedIds.stream().distinct().count() != orderedIds.size()) {
            throw new ProjectException(CourseErrorCode.INVALID_PLACE_ORDER);
        }

        getCourseAsMember(userId, courseId);

        List<CoursePlace> coursePlaces = coursePlaceRepository.findByCourse_CourseIdOrderByPlaceOrderAsc(courseId);
        if (orderedIds.size() != coursePlaces.size()) {
            throw new ProjectException(CourseErrorCode.INVALID_PLACE_ORDER);
        }

        Map<Long, CoursePlace> byId = new HashMap<>();
        for (CoursePlace cp : coursePlaces) {
            byId.put(cp.getCoursePlaceId(), cp);
        }

        int order = 1;
        for (Long coursePlaceId : orderedIds) {
            CoursePlace target = byId.get(coursePlaceId);
            if (target == null) {
                throw new ProjectException(CourseErrorCode.COURSE_PLACE_NOT_FOUND);
            }
            target.changeOrder(order++);
        }

        return TodayCourseConverter.toCoursePlaces(coursePlaceRepository.findPlacesByCourseId(courseId));
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
