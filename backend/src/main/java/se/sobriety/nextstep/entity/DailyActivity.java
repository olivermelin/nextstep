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
        name = "uk_daily_activity_user_date_type",
        columnNames = {"userId", "activityDate", "activityType"}
    ),
    indexes = {
        @Index(name = "idx_daily_activity_user_id", columnList = "userId"),
        @Index(name = "idx_daily_activity_user_date", columnList = "userId, activityDate")
    }
)
public class DailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private int count;

    /** Valfri data, t.ex. mood för CHECK_IN */
    @Column(length = 100)
    private String metadata;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected DailyActivity() {
    }

    public DailyActivity(String userId, LocalDate date, ActivityType type) {
        this.userId = userId;
        this.activityDate = date;
        this.activityType = type;
        this.count = 1;
    }

    public void incrementCount() {
        this.count++;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
