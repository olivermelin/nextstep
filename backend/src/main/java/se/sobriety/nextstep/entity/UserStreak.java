package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Håller streak-data för en användare.
 * En streak räknas som ett sammanhängande antal dagar med minst en slutförd aktivitet.
 * Streaken bryts om användaren inte slutfört någon aktivitet igår eller idag.
 */
@Entity
@Getter
@Table(indexes = @Index(name = "idx_user_streak_user_id", columnList = "userId"))
public class UserStreak extends BaseUserData {

    /** Antal dagar i nuvarande streak */
    @Column(nullable = false)
    private int currentStreak = 0;

    /** Längsta streak användaren någonsin haft */
    @Column(nullable = false)
    private int longestStreak = 0;

    /** Datum för senaste registrerade aktivitet */
    @Column
    private LocalDate lastActivityDate;

    /** Datum då nuvarande streak startade */
    @Column
    private LocalDate streakStartDate;

    protected UserStreak() {}

    public UserStreak(String userId) {
        super(userId);
        this.currentStreak = 0;
        this.longestStreak = 0;
    }

    /**
     * Registrerar aktivitet för idag och uppdaterar streak.
     * Kallas när en challenge slutförs eller en daglig check-in görs.
     *
     * @return true om streaken ökade (ny dag registrerad), false om idag redan räknats
     */
    public boolean registerActivity() {
        LocalDate today = LocalDate.now();

        // Redan registrerat idag — ingen ändring
        if (lastActivityDate != null && lastActivityDate.equals(today)) {
            return false;
        }

        if (lastActivityDate != null && lastActivityDate.equals(today.minusDays(1))) {
            // Fortsatt streak — dagen direkt efter förra aktiviteten
            currentStreak++;
        } else {
            // Bruten streak eller första aktiviteten — börja om från 1
            currentStreak = 1;
            streakStartDate = today;
        }

        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }

        lastActivityDate = today;
        return true;
    }

    /**
     * Kontrollerar om streaken är bruten (ingen aktivitet igår eller idag).
     */
    public boolean isStreakBroken() {
        if (lastActivityDate == null) return false;
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return lastActivityDate.isBefore(yesterday);
    }

    /**
     * Återställer streaken (används om streaken bröts).
     */
    public void resetStreak() {
        this.currentStreak = 0;
        this.streakStartDate = null;
    }
}
