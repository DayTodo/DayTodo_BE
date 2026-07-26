package com.daytodo.domain.place.entity.mapping;
import com.daytodo.domain.common.BaseCreatedEntity;
import com.daytodo.domain.place.entity.Place;
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
        name = "bookmark_place",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookmark_place_user_place",
                        columnNames = {
                                "user_id",
                                "place_id"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkPlace extends BaseCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_place_id")
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="place_id", nullable = false)
    private Place place;

    public BookmarkPlace(
            User user,
            Place place
    ) {
        this.user = user;
        this.place = place;
    }
}
