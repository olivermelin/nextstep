package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Spårar antalet AI-meddelanden en användare skickat under ett dygn.
 * Nollställs automatiskt dag för dag via quotaDatum.
 */
@Getter
@Setter
@Entity
@Table(
    name = "daily_message_quota",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_quota_user_date",
        columnNames = {"userId", "quotaDate"}
    ),
    indexes = @Index(name = "idx_quota_user_date", columnList = "userId, quotaDate")
)
public class DailyMessageQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate quotaDate;

    @Column(nullable = false)
    private int messageCount = 0;

    protected DailyMessageQuota() {}

    public DailyMessageQuota(String userId, LocalDate quotaDate) {
        this.userId = userId;
        this.quotaDate = quotaDate;
        this.messageCount = 0;
    }

    public void increment() {
        this.messageCount++;
    }
}
