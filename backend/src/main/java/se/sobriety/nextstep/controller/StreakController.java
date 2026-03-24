package se.sobriety.nextstep.controller;

import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.StreakResponseDto;
import se.sobriety.nextstep.service.StreakService;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/streaks")
public class StreakController {

    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    /**
     * GET /api/streaks/{userId}
     * Hämta streak-data för en användare
     */
    @GetMapping("/{userId}")
    public StreakResponseDto getStreak(@PathVariable String userId) {
        verifyUserAccess(userId);
        return streakService.getStreak(userId);
    }

    /**
     * POST /api/streaks/{userId}/activity
     * Registrera aktivitet manuellt (används av frontend vid behov)
     * Normalt triggas detta automatiskt vid slutförd challenge.
     */
    @PostMapping("/{userId}/activity")
    public StreakResponseDto registerActivity(@PathVariable String userId) {
        verifyUserAccess(userId);
        return streakService.registerActivity(userId);
    }
}
