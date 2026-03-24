package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uk_daily_reward_user_date",
        columnNames = {"userId", "rewardDate"}
    ),
    indexes = @Index(name = "idx_daily_reward_user_id", columnList = "userId")
)
public class DailyReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate rewardDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType rewardType;

    /** JSON-data för belöningen, t.ex. {"xp": 25} eller {"quoteId": "q3"} */
    @Column(length = 500)
    private String rewardValue;

    @Column(nullable = false)
    private boolean claimed;

    /** Antal konsekutiva dagar med check-in vid tidpunkten för generering */
    @Column(nullable = false)
    private int consecutiveDays;

    /** Om belöningen är sällsynt (guld) */
    @Column(nullable = false)
    private boolean rare;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected DailyReward() {
    }

    public DailyReward(String userId, LocalDate date, RewardType type, String value,
                       int consecutiveDays, boolean rare) {
        this.userId = userId;
        this.rewardDate = date;
        this.rewardType = type;
        this.rewardValue = value;
        this.consecutiveDays = consecutiveDays;
        this.rare = rare;
        this.claimed = false;
    }

    public void claim() {
        this.claimed = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
