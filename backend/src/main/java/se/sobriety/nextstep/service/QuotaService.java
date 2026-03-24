package se.sobriety.nextstep.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import se.sobriety.nextstep.dto.QuotaResponseDto;
import se.sobriety.nextstep.entity.DailyMessageQuota;
import se.sobriety.nextstep.entity.SubscriptionTier;
import se.sobriety.nextstep.entity.UserSettings;
import se.sobriety.nextstep.exception.QuotaExceededException;
import se.sobriety.nextstep.repository.DailyMessageQuotaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hanterar daglig meddelandekvot för AI-coachen.
 *
 * FREE-användare: 10 meddelanden/dag
 * PREMIUM-användare: obegränsat
 *
 * Kvoten nollställs automatiskt varje midnatt (Stockholm-tid).
 */
@Service
public class QuotaService {

    public static final int FREE_DAILY_LIMIT = 10;
    private static final ZoneId SWEDISH_ZONE = ZoneId.of("Europe/Stockholm");

    private final DailyMessageQuotaRepository quotaRepository;
    private final UserSettingsService userSettingsService;

    public QuotaService(DailyMessageQuotaRepository quotaRepository,
                        UserSettingsService userSettingsService) {
        this.quotaRepository = quotaRepository;
        this.userSettingsService = userSettingsService;
    }

    /**
     * Kontrollerar om användaren har kvot kvar och ökar räknaren.
     * Kastas {@link QuotaExceededException} om FREE-användaren har nått gränsen.
     * Gör ingenting för PREMIUM-användare.
     *
     * @param userId användar-ID
     * @throws QuotaExceededException om gränsen är uppnådd
     */
    @Transactional
    public void checkAndIncrement(String userId) {
        UserSettings settings = userSettingsService.getOrCreateSettings(userId);

        // PREMIUM: ingen kvot
        if (settings.getSubscriptionTier() == SubscriptionTier.PREMIUM) {
            return;
        }

        LocalDate today = LocalDate.now(SWEDISH_ZONE);
        DailyMessageQuota quota = quotaRepository
                .findByUserIdAndQuotaDate(userId, today)
                .orElseGet(() -> quotaRepository.save(new DailyMessageQuota(userId, today)));

        if (quota.getMessageCount() >= FREE_DAILY_LIMIT) {
            throw new QuotaExceededException(quota.getMessageCount(), FREE_DAILY_LIMIT);
        }

        quota.increment();
        // JPA dirty checking sparar automatiskt
    }

    /**
     * Returnerar aktuell kvotstatus utan att öka räknaren.
     */
    @Transactional
    public QuotaResponseDto getQuota(String userId) {
        UserSettings settings = userSettingsService.getOrCreateSettings(userId);
        boolean isPremium = settings.getSubscriptionTier() == SubscriptionTier.PREMIUM;

        if (isPremium) {
            return new QuotaResponseDto(0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, resetAtString());
        }

        LocalDate today = LocalDate.now(SWEDISH_ZONE);
        int used = quotaRepository
                .findByUserIdAndQuotaDate(userId, today)
                .map(DailyMessageQuota::getMessageCount)
                .orElse(0);

        int remaining = Math.max(0, FREE_DAILY_LIMIT - used);
        return new QuotaResponseDto(used, remaining, FREE_DAILY_LIMIT, false, resetAtString());
    }

    /**
     * Raderar all kvot-data för en användare (GDPR artikel 17).
     */
    @Transactional
    public void deleteAllForUser(String userId) {
        quotaRepository.deleteByUserId(userId);
    }

    private String resetAtString() {
        ZonedDateTime nextMidnight = ZonedDateTime.now(SWEDISH_ZONE)
                .with(LocalTime.MIDNIGHT)
                .plusDays(1);
        return nextMidnight.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
