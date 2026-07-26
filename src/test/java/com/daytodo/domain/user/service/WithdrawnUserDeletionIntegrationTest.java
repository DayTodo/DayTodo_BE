package com.daytodo.domain.user.service;

import com.daytodo.domain.course.entity.Course;
import com.daytodo.domain.course.entity.CourseMember;
import com.daytodo.domain.course.entity.InviteCode;
import com.daytodo.domain.course.enums.MemberRole;
import com.daytodo.domain.course.enums.MemberStatus;
import com.daytodo.domain.course.enums.ParticipantType;
import com.daytodo.domain.course.repository.CourseMemberRepository;
import com.daytodo.domain.course.repository.CourseRepository;
import com.daytodo.domain.course.repository.InviteCodeRepository;
import com.daytodo.domain.region.entity.Region;
import com.daytodo.domain.region.enums.RegionLevel;
import com.daytodo.domain.region.repository.RegionRepository;
import com.daytodo.domain.user.entity.User;
import com.daytodo.domain.user.enums.LoginType;
import com.daytodo.domain.user.repository.UserRepository;
import com.daytodo.domain.user.repository.WithdrawnUserCleanupRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WithdrawnUserDeletionIntegrationTest {
    @Autowired UserRepository userRepository;
    @Autowired RegionRepository regionRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired CourseMemberRepository courseMemberRepository;
    @Autowired InviteCodeRepository inviteCodeRepository;
    @Autowired WithdrawnUserCleanupRepository cleanupRepository;
    @Autowired EntityManager entityManager;

    @Test
    void permanentlyDeletesUserWithoutDeletingSharedCourse() {
        User owner = userRepository.save(new User("owner@example.com", null, "owner", null, LoginType.NAVER));
        User member = userRepository.save(new User("member@example.com", null, "member", null, LoginType.NAVER));
        Region seoul = regionRepository.save(new Region(null, "서울특별시", RegionLevel.SIDO));
        Region gangnam = regionRepository.save(new Region(seoul, "강남구", RegionLevel.SIGUNGU));
        Course course = courseRepository.save(new Course(
                owner,
                gangnam,
                "공유 코스",
                LocalDate.of(2026, 8, 1),
                0,
                10000,
                ParticipantType.FRIEND
        ));
        courseMemberRepository.save(new CourseMember(course, owner, MemberRole.OWNER, MemberStatus.JOINED));
        courseMemberRepository.save(new CourseMember(course, member, MemberRole.MEMBER, MemberStatus.JOINED));
        inviteCodeRepository.save(new InviteCode(
                course,
                owner,
                "SHARED123",
                LocalDateTime.of(2026, 8, 1, 23, 59)
        ));
        owner.withdraw(LocalDateTime.of(2026, 6, 22, 10, 0));
        entityManager.flush();
        entityManager.clear();

        Clock clock = Clock.fixed(Instant.parse("2026-07-22T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        WithdrawnUserDeletionService service = new WithdrawnUserDeletionService(
                userRepository,
                cleanupRepository,
                clock
        );
        int deleted = service.deleteExpiredWithdrawnUsers();
        entityManager.flush();
        entityManager.clear();

        assertThat(deleted).isOne();
        assertThat(userRepository.findById(owner.getId())).isEmpty();
        Course preserved = courseRepository.findById(course.getCourseId()).orElseThrow();
        assertThat(preserved.getOwner()).isNull();
        assertThat(courseMemberRepository.findByCourseCourseIdAndUserId(course.getCourseId(), member.getId()))
                .isPresent();
    }
}
