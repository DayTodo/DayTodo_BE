package com.daytodo.domain.place.entity;
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
        name = "bookmark_category",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookmark_category_user_name",
                        columnNames = {
                                "user_id",
                                "category_name"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_category_id")
    private Long id;

    /*
     * 사용자 한 명은 여러 북마크 카테고리를 만들 수 있습니다.
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

    @Column(
            name = "category_name",
            length = 50,
            nullable = false
    )
    private String categoryName;

    @Column(
            name = "is_default",
            nullable = false
    )
    private boolean defaultCategory;

    public BookmarkCategory(
            User user,
            String categoryName,
            boolean defaultCategory
    ) {
        this.user = user;
        this.categoryName = categoryName;
        this.defaultCategory = defaultCategory;
    }

    public void rename(String categoryName) {
        this.categoryName = categoryName;
    }

    public void changeDefault(boolean defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

}
