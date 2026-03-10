package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity för att tracka framsteg per kategori för en användare
 */
@Entity
@Getter
@Setter
public class CategoryProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeCategory category;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    protected CategoryProgress() {
    }

    public CategoryProgress(String userId, ChallengeCategory category) {
        this.userId = userId;
        this.category = category;
        this.points = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public void addPoints(int pointsToAdd) {
        if (pointsToAdd > 0) {
            this.points += pointsToAdd;
            this.lastUpdated = LocalDateTime.now();
        }
    }
}

