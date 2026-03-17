package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_user_progress_user_id", columnList = "userId"))
public class UserProgress extends BaseUserData {

    @Column(nullable = false)
    private int totalPoints;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    protected UserProgress() {
    }

    public UserProgress(String userId) {
        this.userId = userId;
        this.totalPoints = 0;
        this.level = 1;
        this.lastUpdated = LocalDateTime.now();
    }


    public void applyPoints(int points) {
        if (points <= 0) {
            return;
        }
        this.totalPoints += points;
        touch();
    }

    public void updateLevel(int newLevel) {
        if (newLevel > this.level) {
            this.level = newLevel;
            touch();
        }
    }

    private void touch() {
        this.lastUpdated = LocalDateTime.now();
    }
}
