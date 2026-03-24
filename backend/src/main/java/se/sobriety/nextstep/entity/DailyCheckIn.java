package se.sobriety.nextstep.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Daglig incheckning där användaren registrerar sitt mående.
 * Maximalt en incheckning per användare och dag (enforced via unik constraint).
 */
@Entity
@Getter
@Table(
        name = "daily_check_in",
        indexes = @Index(name = "idx_daily_checkin_user_id", columnList = "userId"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_checkin_user_date",
                columnNames = {"userId", "checkInDate"}
        )
)
public class DailyCheckIn extends BaseUserData {

    /** Datum för incheckningen */
    @Column(nullable = false)
    private LocalDate checkInDate;

    /**
     * Humörpoäng 1–5:
     * 1 = Mycket dåligt, 2 = Dåligt, 3 = Okej, 4 = Bra, 5 = Mycket bra
     */
    @Column(nullable = false)
    private int moodScore;

    /** Frivillig anteckning, max 500 tecken */
    @Column(length = 500)
    private String note;

    protected DailyCheckIn() {}

    public DailyCheckIn(String userId, int moodScore, String note) {
        super(userId);
        this.checkInDate = LocalDate.now();
        this.moodScore = moodScore;
        this.note = note;
    }

    /**
     * Uppdatera mood och anteckning om användaren checkar in igen samma dag.
     * (Används om frontend tillåter redigering av dagens incheckning.)
     */
    public void update(int moodScore, String note) {
        this.moodScore = moodScore;
        this.note = note;
    }
}
