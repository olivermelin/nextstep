package se.sobriety.nextstep.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.CheckInRequestDto;
import se.sobriety.nextstep.dto.CheckInResponseDto;
import se.sobriety.nextstep.entity.DailyCheckIn;
import se.sobriety.nextstep.repository.DailyCheckInRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Hanterar dagliga incheckningar för användare.
 * En incheckning per dag tillåts — om användaren checkar in igen samma dag uppdateras befintlig post.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class DailyCheckInService {

    private final DailyCheckInRepository checkInRepository;
    private final StreakService streakService;

    public DailyCheckInService(DailyCheckInRepository checkInRepository, StreakService streakService) {
        this.checkInRepository = checkInRepository;
        this.streakService = streakService;
    }

    /**
     * Checka in idag. Om användaren redan checkat in idag uppdateras befintlig post.
     * Registrerar också aktivitet för streaken.
     */
    @Transactional
    public CheckInResponseDto checkIn(String userId, CheckInRequestDto request) {
        LocalDate today = LocalDate.now();
        Optional<DailyCheckIn> existing = checkInRepository.findByUserIdAndCheckInDate(userId, today);

        DailyCheckIn checkIn;
        boolean alreadyCheckedIn = existing.isPresent();

        if (alreadyCheckedIn) {
            // Uppdatera befintlig incheckning för idag
            checkIn = existing.get();
            checkIn.update(request.moodScore(), request.note());
            log.info("Uppdaterar incheckning för användare {} idag", userId);
        } else {
            // Skapa ny incheckning
            checkIn = new DailyCheckIn(userId, request.moodScore(), request.note());
            // Registrera aktivitet för streaken vid ny incheckning
            streakService.registerActivity(userId);
            log.info("Ny incheckning registrerad för användare {} med mood {}", userId, request.moodScore());
        }

        checkIn = checkInRepository.save(checkIn);
        return toDto(checkIn, alreadyCheckedIn);
    }

    /**
     * Hämta dagens incheckning för användaren (om den finns).
     */
    public Optional<CheckInResponseDto> getTodaysCheckIn(String userId) {
        return checkInRepository.findByUserIdAndCheckInDate(userId, LocalDate.now())
                .map(c -> toDto(c, true));
    }

    /**
     * Hämta incheckningshistorik för användaren (senaste 30 dagar som default).
     */
    public List<CheckInResponseDto> getHistory(String userId, int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();
        return checkInRepository
                .findByUserIdAndCheckInDateBetweenOrderByCheckInDateDesc(userId, from, to)
                .stream()
                .map(c -> toDto(c, false))
                .toList();
    }

    /**
     * Tar bort all check-in-data för en användare (GDPR-radering).
     */
    @Transactional
    public void deleteAllForUser(String userId) {
        checkInRepository.deleteByUserId(userId);
    }

    private CheckInResponseDto toDto(DailyCheckIn checkIn, boolean alreadyCheckedIn) {
        return new CheckInResponseDto(
                checkIn.getId(),
                checkIn.getUserId(),
                checkIn.getCheckInDate(),
                checkIn.getMoodScore(),
                checkIn.getNote(),
                alreadyCheckedIn
        );
    }
}
