package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uk_collectible_user_key",
        columnNames = {"userId", "collectibleKey"}
    ),
    indexes = @Index(name = "idx_collectible_user_id", columnList = "userId")
)
public class Collectible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String userId;

    /** Unik nyckel för samlingsobjektet, t.ex. "quote_01", "frame_gold" */
    @Column(nullable = false, length = 100)
    private String collectibleKey;

    /** Typ: QUOTE_CARD eller AVATAR_FRAME */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType collectibleType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime unlockedAt;

    protected Collectible() {
    }

    public Collectible(String userId, String key, RewardType type) {
        this.userId = userId;
        this.collectibleKey = key;
        this.collectibleType = type;
    }

    @PrePersist
    protected void onCreate() {
        this.unlockedAt = LocalDateTime.now();
    }
}
