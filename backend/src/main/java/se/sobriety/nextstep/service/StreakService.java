package se.sobriety.nextstep.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.StreakResponseDto;
import se.sobriety.nextstep.entity.UserStreak;
import se.sobriety.nextstep.repository.UserStreakRepository;

import java.time.LocalDate;

/**
 * Hanterar streak-logik för användare.
 * Streaken uppdateras automatiskt när en challenge slutförs.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class StreakService {

    private final UserStreakRepository streakRepository;

    public StreakService(UserStreakRepository streakRepository) {
        this.streakRepository = streakRepository;
    }

    /**
     * Hämta eller skapa streak-data för användaren.
     */
    @Transactional
    public UserStreak getOrCreateStreak(String userId) {
        return streakRepository.findByUserId(userId)
                .orElseGet(() -> streakRepository.save(new UserStreak(userId)));
    }

    /**
     * Hämta streak-data som DTO.
     */
    public StreakResponseDto getStreak(String userId) {
        UserStreak streak = streakRepository.findByUserId(userId)
                .orElse(new UserStreak(userId));
        return toDto(streak);
    }

    /**
     * Registrerar aktivitet för idag (anropas vid slutförd challenge eller check-in).
     * Returnerar uppdaterad streak-data.
     */
    @Transactional
    public StreakResponseDto registerActivity(String userId) {
        UserStreak streak = getOrCreateStreak(userId);
        boolean increased = streak.registerActivity();

        if (increased) {
            log.info("Streak uppdaterad för användare {}: {} dagar", userId, streak.getCurrentStreak());
        }

        return toDto(streakRepository.save(streak));
    }

    /**
     * Tar bort all streak-data för en användare (GDPR-radering).
     */
    @Transactional
    public void deleteAllForUser(String userId) {
        streakRepository.deleteByUserId(userId);
    }

    private StreakResponseDto toDto(UserStreak streak) {
        LocalDate today = LocalDate.now();
        boolean activityToday = today.equals(streak.getLastActivityDate());
        boolean streakActive = activityToday ||
                (streak.getLastActivityDate() != null && streak.getLastActivityDate().equals(today.minusDays(1)));

        return new StreakResponseDto(
                streak.getUserId(),
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getLastActivityDate(),
                streak.getStreakStartDate(),
                streakActive,
                activityToday
        );
    }
}
