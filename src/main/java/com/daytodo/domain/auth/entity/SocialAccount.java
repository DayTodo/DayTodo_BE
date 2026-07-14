package com.daytodo.domain.auth.entity;
import com.daytodo.domain.auth.enums.SocialProvider;
import com.daytodo.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
@Getter
@Entity
@Table(
        name = "social_account",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_account_provider_user_id",
                        columnNames = {
                                "provider",
                                "provider_user_id"
                        }
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long id;

    /*
     * 한 명의 사용자가 여러 소셜 계정을 연결할 수 있으므로
     * SocialAccount N : 1 User 관계입니다.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider",
            length = 20,
            nullable = false
    )
    private SocialProvider provider;

    /*
     * 카카오나 구글에서 제공하는 사용자의 고유 식별자입니다.
     */
    @Column(
            name = "provider_user_id",
            length = 100,
            nullable = false
    )
    private String providerUserId;

    @CreatedDate
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public SocialAccount(
            User user,
            SocialProvider provider,
            String providerUserId
    ) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

}
