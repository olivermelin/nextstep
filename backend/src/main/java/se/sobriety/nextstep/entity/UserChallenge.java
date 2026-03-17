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
@Table(indexes = {
        @Index(name = "idx_user_challenge_user_id", columnList = "userId"),
        @Index(name = "idx_user_challenge_user_completed", columnList = "userId, completed")
})
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

    /** Faktisk tid (minuter) som användaren angav vid slutförande. 0 = ej angiven. */
    @Column(nullable = false)
    private int actualMinutes;

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

    /**
     * Slutför utmaningen och beräknar poäng baserat på svårighetsgrad och faktisk tid.
     *
     * @param actualMinutes faktisk tid som användaren spenderade (0 = använd standardtid)
     */
    public void complete(int actualMinutes) {
        this.completed = true;
        this.completedAt = LocalDateTime.now();

        int defaultMinutes = this.challenge.getDurationMinutes();
        this.actualMinutes = actualMinutes > 0 ? actualMinutes : defaultMinutes;

        int basePoints = this.challenge.getDifficulty().getPoints();
        // Tidsskalning: 50 % av baspoäng minimum, 300 % maximum
        double ratio = (double) this.actualMinutes / Math.max(1, defaultMinutes);
        double clamped = Math.max(0.5, Math.min(3.0, ratio));
        this.pointsEarned = (int) Math.round(basePoints * clamped);
    }

    @PrePersist
    protected void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }
}

