package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity för challenges/utmaningar som användare kan genomföra
 */
@Entity
@Getter
@Setter
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeCategory category;

    @Column(length = 500)
    private String youtubeUrl;

    @Column(length = 2000)
    private String instructions;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Challenge() {
    }

    public Challenge(String title, String description, int durationMinutes,
                     ChallengeDifficulty difficulty, ChallengeCategory category) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.difficulty = difficulty;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}

