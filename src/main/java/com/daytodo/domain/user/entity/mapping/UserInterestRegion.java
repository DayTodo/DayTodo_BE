package com.daytodo.domain.user.entity.mapping;
import com.daytodo.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@Entity
@Table(
        name = "user_interest_region",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_interest_region",
                        columnNames = {
                                "user_id",
                                "region_id"
                        }
                )
        }
)
public class UserInterestRegion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_interest_region_id")
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /*
     * Region 엔티티가 아직 없으므로 현재는 ID만 저장합니다.
     *
     * Region 엔티티가 병합된 후에는 다음과 같이 변경할 수 있습니다.
     *
     * @ManyToOne(fetch = FetchType.LAZY)
     * @JoinColumn(name = "region_id")
     * private Region region;
     */
    @Column(
            name = "region_id",
            nullable = false
    )
    private Long regionId;

    public UserInterestRegion(
            User user,
            Long regionId
    ) {
        this.user = user;
        this.regionId = regionId;
    }
}
