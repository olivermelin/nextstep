package se.sobriety.nextstep.controller;

import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.AchievementDto;
import se.sobriety.nextstep.dto.CategoryProgressResponseDto;
import se.sobriety.nextstep.dto.ProgressResponseDto;
import se.sobriety.nextstep.entity.UserProgress;
import se.sobriety.nextstep.service.UserProgressService;

import java.util.List;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final UserProgressService service;

    public ProgressController(UserProgressService service) {
        this.service = service;
    }

    /**
     * GET /api/progress/{userId}
     * Hämta användarens totala framsteg
     */
    @GetMapping("/{userId}")
    public ProgressResponseDto getProgress(@PathVariable String userId) {
        verifyUserAccess(userId);
        return service.getTotalProgress(userId);
    }

    /**
     * GET /api/progress/{userId}/categories?days={antal}
     * Hämta framsteg per kategori för en viss period (default 7 dagar)
     */
    @GetMapping("/{userId}/categories")
    public CategoryProgressResponseDto getCategoryProgress(
            @PathVariable String userId,
            @RequestParam(defaultValue = "7") int days) {
        verifyUserAccess(userId);
        if (days <= 0 || days > 365) {
            throw new IllegalArgumentException("Days must be between 1 and 365");
        }
        return service.getCategoryProgress(userId, days);
    }

    /**
     * GET /api/progress/{userId}/achievements
     * Hämta användarens achievements
     */
    @GetMapping("/{userId}/achievements")
    public List<AchievementDto> getAchievements(@PathVariable String userId) {
        verifyUserAccess(userId);
        return service.getUserAchievements(userId);
    }

    /**
     * POST /api/progress/{userId}/add?points={poäng}
     * Lägg till poäng för användaren och uppdatera nivå automatiskt
     */
    @PostMapping("/{userId}/add")
    public ProgressResponseDto addPoints(
            @PathVariable String userId,
            @RequestParam int points) {
        verifyUserAccess(userId);
        if (points <= 0 || points > 100) {
            throw new IllegalArgumentException("Invalid points: must be between 1 and 100");
        }
        UserProgress updated = service.addPoints(userId, points);
        return new ProgressResponseDto(
                updated.getUserId(),
                updated.getTotalPoints(),
                updated.getLevel(),
                updated.getLastUpdated().toString()
        );
    }

    // Legacy endpoints (kept for backwards compatibility)

    @PostMapping("/{userId}/create")
    public UserProgress createUser(@PathVariable String userId) {
        verifyUserAccess(userId);
        return service.createUser(userId);
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable String userId) {
        verifyUserAccess(userId);
        service.deleteUser(userId);
        return "User " + userId + " deleted (if existed)";
    }

}
