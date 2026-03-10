package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity för att tracka användarens challenges
 */
@Entity
@Getter
@Setter
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false)
    private boolean completed;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private int pointsEarned;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    protected UserChallenge() {
    }

    public UserChallenge(String userId, Challenge challenge) {
        this.userId = userId;
        this.challenge = challenge;
        this.completed = false;
        this.pointsEarned = 0;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
        this.pointsEarned = this.challenge.getDifficulty().getPoints();
    }

    @PrePersist
    protected void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }
}

