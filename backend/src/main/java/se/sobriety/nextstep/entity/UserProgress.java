package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_user_progress_user_id", columnList = "userId"))
public class UserProgress extends BaseUserData {

    @Column(nullable = false)
    private int totalPoints;

    @Column(nullable = false)
    private int level;

    protected UserProgress() {
    }

    public UserProgress(String userId) {
        super(userId);
        this.totalPoints = 0;
        this.level = 1;
    }

    public void applyPoints(int points) {
        if (points <= 0) {
            return;
        }
        this.totalPoints += points;
    }

    public void updateLevel(int newLevel) {
        if (newLevel > this.level) {
            this.level = newLevel;
        }
    }
}
