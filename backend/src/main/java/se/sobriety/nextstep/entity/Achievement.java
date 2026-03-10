package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity för achievements/prestationer
 */
@Entity
@Getter
@Setter
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int achievementId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private int requiredPoints;

    @Column(nullable = false)
    private int requiredLevel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Achievement() {
    }

    public Achievement(int achievementId, String title, String description, int requiredPoints, int requiredLevel) {
        this.achievementId = achievementId;
        this.title = title;
        this.description = description;
        this.requiredPoints = requiredPoints;
        this.requiredLevel = requiredLevel;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}

