package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Heja-reaktion på ett feed-inlägg.
 * En användare kan bara heja på ett inlägg en gång (enforced via unique constraint).
 */
@Getter
@Setter
@Entity
@Table(name = "feed_reaction",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_feed_reaction_user_item",
                columnNames = {"userId", "feedItemId"}
        ),
        indexes = @Index(name = "idx_feed_reaction_item", columnList = "feedItemId")
)
public class FeedReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private Long feedItemId;

    /** Cackat visningsnamn */
    @Column(nullable = false, updatable = false)
    private String displayName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected FeedReaction() {}

    public FeedReaction(String userId, Long feedItemId, String displayName) {
        this.userId      = userId;
        this.feedItemId  = feedItemId;
        this.displayName = displayName;
        this.createdAt   = LocalDateTime.now();
    }
}
