package com.daytodo.domain.user.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WithdrawnUserCleanupRepository {
    private final EntityManager entityManager;

    public void removeUserReferences(Long userId) {
        delete("delete from RecommendationLike entity where entity.userId = :userId", userId);
        delete("delete from RecommendationComment entity where entity.userId = :userId", userId);
        update("update PlaceRecommendation entity set entity.recommenderId = null where entity.recommenderId = :userId", userId);

        delete("""
                delete from BookmarkPlace entity
                where entity.user.id = :userId or entity.bookmarkCategory.user.id = :userId
                """, userId);
        delete("delete from BookmarkCategory entity where entity.user.id = :userId", userId);
        delete("delete from SocialAccount entity where entity.user.id = :userId", userId);
        delete("delete from Notification entity where entity.user.id = :userId", userId);
        delete("delete from UserNotificationSetting entity where entity.user.id = :userId", userId);
        delete("delete from Feedback entity where entity.user.id = :userId", userId);
        delete("delete from UserInterestRegion entity where entity.user.id = :userId", userId);
        delete("delete from Diary entity where entity.user.id = :userId", userId);
        delete("delete from CourseMember entity where entity.user.id = :userId", userId);

        update("update InviteCode entity set entity.creator = null where entity.creator.id = :userId", userId);
        update("update CoursePlace entity set entity.addedBy = null where entity.addedBy.id = :userId", userId);
        update("update Course entity set entity.owner = null where entity.owner.id = :userId", userId);
    }

    private void delete(String jpql, Long userId) {
        entityManager.createQuery(jpql)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    private void update(String jpql, Long userId) {
        entityManager.createQuery(jpql)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
