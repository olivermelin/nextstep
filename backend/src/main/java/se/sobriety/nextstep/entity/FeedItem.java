package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Automatiskt genererat feed-inlägg som visas i vänners flöde.
 * Inlägg är oföränderliga — användaren kan bara soft-radera egna inlägg.
 */
@Getter
@Setter
@Entity
@Table(name = "feed_item", indexes = {
        @Index(name = "idx_feed_item_user_id",    columnList = "userId"),
        @Index(name = "idx_feed_item_created_at", columnList = "createdAt"),
        @Index(name = "idx_feed_item_user_del",   columnList = "userId, deleted")
})
public class FeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private FeedItemType type;

    /** Referens till källentiteten (UserChallenge.id, Duel.id osv.) */
    @Column(updatable = false)
    private Long referenceId;

    /** Förrenderat meddelande, t.ex. "Anna klarade '10 min meditation'" */
    @Column(nullable = false, updatable = false, length = 500)
    private String message;

    /** Cachat visningsnamn vid skapandetillfället */
    @Column(nullable = false, updatable = false)
    private String displayName;

    /** Extra metadata som JSON (kategori, poäng, streak-antal osv.) */
    @Column(updatable = false, length = 1000)
    private String metadata;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Soft-radering: användaren kan dölja egna inlägg */
    @Column(nullable = false)
    private boolean deleted = false;

    protected FeedItem() {}

    public FeedItem(String userId, FeedItemType type, Long referenceId,
                    String message, String displayName, String metadata) {
        this.userId      = userId;
        this.type        = type;
        this.referenceId = referenceId;
        this.message     = message;
        this.displayName = displayName;
        this.metadata    = metadata;
        this.createdAt   = LocalDateTime.now();
        this.deleted     = false;
    }

    public void softDelete() {
        this.deleted = true;
    }
}
