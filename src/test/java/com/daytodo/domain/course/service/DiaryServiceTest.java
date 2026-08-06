package com.daytodo.domain.course.service;

import com.daytodo.domain.course.dto.DiaryRequest;
import com.daytodo.domain.course.dto.DiaryResponse;
import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.Diary;
import com.daytodo.domain.course.entity.MemoryPhoto;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.course.exception.code.CourseErrorCode;
import com.daytodo.domain.course.exception.code.DiaryErrorCode;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.course.repository.CoursePlaceRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import com.daytodo.domain.course.repository.DiaryRepository;
import com.daytodo.domain.course.repository.MemoryPhotoRepository;
import com.daytodo.domain.place.repository.PlaceRepository;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.enums.UserStatus;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 10L;

    @Mock DiaryRepository diaryRepository;
    @Mock MemoryPhotoRepository memoryPhotoRepository;
    @Mock CourseRepository courseRepository;
    @Mock CoursePlaceRepository coursePlaceRepository;
    @Mock PlaceRepository placeRepository;
    @Mock UserRepository userRepository;
    @Mock CourseMemberRepository courseMemberRepository;

    DiaryService diaryService;
    User user;
    Course course;

    @BeforeEach
    void setUp() {
        diaryService = new DiaryService(
                diaryRepository,
                memoryPhotoRepository,
                courseRepository,
                coursePlaceRepository,
                placeRepository,
                userRepository,
                courseMemberRepository
        );

        user = new User("user@example.com", "password", "user", null, LoginType.LOCAL);
        ReflectionTestUtils.setField(user, "id", USER_ID);

        course = new Course(user, null, "코스", LocalDate.of(2026, 7, 20), 0, 30000, ParticipantType.FRIEND);
        ReflectionTestUtils.setField(course, "courseId", COURSE_ID);
        course.complete();
    }

    @Test
    void 일기를_새로_작성하면_diary가_생성되고_사진_연동_로직은_더이상_호출되지_않는다() {
        // given: 사진(MemoryPhoto)은 diary와 연관관계가 없어졌으므로(코스 공용, PR #49
        // 리뷰에서 준열님이 지적한 "일기 이후 사진 추가" 순서 문제의 근본 원인 제거),
        // writeDiary()는 diary 레코드만 만들고 memoryPhotoRepository는 건드리지 않는다.
        when(userRepository.findByIdAndUserStatus(USER_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));
        when(diaryRepository.findByUserIdAndCourse_CourseId(USER_ID, COURSE_ID)).thenReturn(Optional.empty());

        Diary savedDiary = Diary.builder()
                .user(user)
                .course(course)
                .diaryDate(course.getCourseDate())
                .content("오늘 재밌었다")
                .build();
        ReflectionTestUtils.setField(savedDiary, "id", 100L);
        when(diaryRepository.save(any(Diary.class))).thenReturn(savedDiary);

        DiaryRequest.Write request = new DiaryRequest.Write(COURSE_ID, "오늘 재밌었다");

        // when
        DiaryResponse.Write response = diaryService.writeDiary(USER_ID, request);

        // then
        assertThat(response.diaryId()).isEqualTo(100L);
        assertThat(response.courseId()).isEqualTo(COURSE_ID);
        verifyNoInteractions(memoryPhotoRepository);
    }

    @Test
    void 이미_작성된_일기가_있으면_내용만_수정하고_새로_저장하지_않는다() {
        // given
        when(userRepository.findByIdAndUserStatus(USER_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(course));

        Diary existingDiary = Diary.builder()
                .user(user)
                .course(course)
                .diaryDate(course.getCourseDate())
                .content("이전 내용")
                .build();
        ReflectionTestUtils.setField(existingDiary, "id", 200L);
        when(diaryRepository.findByUserIdAndCourse_CourseId(USER_ID, COURSE_ID))
                .thenReturn(Optional.of(existingDiary));

        DiaryRequest.Write request = new DiaryRequest.Write(COURSE_ID, "수정된 내용");

        // when
        DiaryResponse.Write response = diaryService.writeDiary(USER_ID, request);

        // then
        assertThat(response.content()).isEqualTo("수정된 내용");
        verify(diaryRepository, never()).save(any(Diary.class));
        verifyNoInteractions(memoryPhotoRepository);
    }

    @Test
    void 코스_멤버는_diary_소유자가_아니어도_추억사진을_조회할_수_있다() {
        // given: 사진은 다른 멤버(=1L이 아닌 다른 유저)의 diary에 연결돼 있어도,
        // 요청자가 코스 멤버이기만 하면 course 단위로 사진을 볼 수 있어야 한다.
        when(userRepository.findByIdAndUserStatus(USER_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(courseMemberRepository.existsByCourseCourseIdAndUserIdAndMemberStatus(
                COURSE_ID, USER_ID, MemberStatus.JOINED
        )).thenReturn(true);

        MemoryPhoto photo = MemoryPhoto.builder()
                .course(course)
                .imageUrl("https://example.com/shared.jpg")
                .photoOrder(1)
                .build();
        when(memoryPhotoRepository.findAllByCourse_CourseIdOrderByPhotoOrderAsc(COURSE_ID))
                .thenReturn(List.of(photo));

        // when
        DiaryResponse.Photos response = diaryService.getPhotosByCourse(USER_ID, COURSE_ID);

        // then
        assertThat(response.courseId()).isEqualTo(COURSE_ID);
        assertThat(response.photos()).hasSize(1);
    }

    @Test
    void 코스_멤버가_아니면_추억사진_조회시_접근이_거부된다() {
        // given
        when(userRepository.findByIdAndUserStatus(USER_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(courseMemberRepository.existsByCourseCourseIdAndUserIdAndMemberStatus(
                COURSE_ID, USER_ID, MemberStatus.JOINED
        )).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> diaryService.getPhotosByCourse(USER_ID, COURSE_ID))
                .isInstanceOf(ProjectException.class)
                .satisfies(exception -> assertThat(((ProjectException) exception).getErrorCode())
                        .isEqualTo(CourseErrorCode.COURSE_ACCESS_DENIED));
    }

    @Test
    void 날짜별_추억_조회시_지금도_코스_JOINED_멤버면_공용_사진을_볼_수_있다() {
        // given
        when(userRepository.findByIdAndUserStatus(USER_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        LocalDate date = course.getCourseDate();

        Diary diary = Diary.builder()
                .user(user)
                .course(course)
                .diaryDate(date)
                .content("오늘 기록")
                .build();
        ReflectionTestUtils.setField(diary, "id", 300L);
        when(diaryRepository.findAllByUserIdAndDiaryDate(USER_ID, date)).thenReturn(List.of(diary));
        when(courseMemberRepository.existsByCourseCourseIdAndUserIdAndMemberStatus(
                COURSE_ID, USER_ID, MemberStatus.JOINED
        )).thenReturn(true);
        when(memoryPhotoRepository.findAllByCourse_CourseIdOrderByPhotoOrderAsc(COURSE_ID))
                .thenReturn(List.of());

        // when
        DiaryResponse.MemoryByDate response = diaryService.getMemoryByDate(USER_ID, date);

        // then
        assertThat(response.diaryId()).isEqualTo(300L);
    }

    @Test
    void 일기를_썼더라도_지금은_코스에서_나간_멤버면_날짜별_추억_조회가_거부된다() {
        // given: diary는 유저가 코스를 나가도(LEFT) 그대로 남아있으므로, diary 소유
        // 여부만으로 공용 사진까지 계속 보여주면 탈퇴 멤버에게도 사진이 새어나간다.
        when(userRepository.findByIdAndUserStatus(USER_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        LocalDate date = course.getCourseDate();

        Diary diary = Diary.builder()
                .user(user)
                .course(course)
                .diaryDate(date)
                .content("예전에 쓴 기록")
                .build();
        ReflectionTestUtils.setField(diary, "id", 301L);
        when(diaryRepository.findAllByUserIdAndDiaryDate(USER_ID, date)).thenReturn(List.of(diary));
        when(courseMemberRepository.existsByCourseCourseIdAndUserIdAndMemberStatus(
                COURSE_ID, USER_ID, MemberStatus.JOINED
        )).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> diaryService.getMemoryByDate(USER_ID, date))
                .isInstanceOf(ProjectException.class)
                .satisfies(exception -> assertThat(((ProjectException) exception).getErrorCode())
                        .isEqualTo(CourseErrorCode.COURSE_ACCESS_DENIED));
    }

    @Test
    void 완료되지_않은_코스면_일기를_작성할_수_없다() {
        // given
        Course inProgressCourse = new Course(
                user, null, "코스", LocalDate.of(2026, 7, 20), 0, 30000, ParticipantType.FRIEND
        );
        ReflectionTestUtils.setField(inProgressCourse, "courseId", COURSE_ID);

        when(userRepository.findByIdAndUserStatus(USER_ID, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(courseRepository.findById(COURSE_ID)).thenReturn(Optional.of(inProgressCourse));

        DiaryRequest.Write request = new DiaryRequest.Write(COURSE_ID, "내용");

        // when & then
        assertThatThrownBy(() -> diaryService.writeDiary(USER_ID, request))
                .isInstanceOf(ProjectException.class)
                .satisfies(exception -> assertThat(((ProjectException) exception).getErrorCode())
                        .isEqualTo(DiaryErrorCode.COURSE_NOT_COMPLETED));
    }
}