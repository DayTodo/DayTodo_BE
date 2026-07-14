package com.daytodo.domain.place.entity.mapping;
import com.daytodo.domain.place.entity.BookmarkCategory;
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
public class BookmarkPlace {
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

    /*
     * Place 엔티티가 아직 없으므로 현재는 placeId만 저장합니다.
     */
    @Column(
            name = "place_id",
            nullable = false
    )
    private Long placeId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "bookmark_category_id",
            nullable = false
    )
    private BookmarkCategory bookmarkCategory;

    public BookmarkPlace(
            User user,
            Long placeId,
            BookmarkCategory bookmarkCategory
    ) {
        this.user = user;
        this.placeId = placeId;
        this.bookmarkCategory = bookmarkCategory;
    }

    public void changeCategory(
            BookmarkCategory bookmarkCategory
    ) {
        this.bookmarkCategory = bookmarkCategory;
    }
}
