package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity för att tracka användarens upplåsta achievements
 */
@Entity
@Getter
@Setter
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(nullable = false)
    private boolean unlocked;

    @Column
    private LocalDateTime unlockedAt;

    protected UserAchievement() {
    }

    public UserAchievement(String userId, Achievement achievement) {
        this.userId = userId;
        this.achievement = achievement;
        this.unlocked = false;
    }

    public void unlock() {
        if (!this.unlocked) {
            this.unlocked = true;
            this.unlockedAt = LocalDateTime.now();
        }
    }
}

