package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.CheckInRequestDto;
import se.sobriety.nextstep.dto.CheckInResponseDto;
import se.sobriety.nextstep.service.DailyCheckInService;

import java.util.List;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/checkins")
public class DailyCheckInController {

    private final DailyCheckInService checkInService;

    public DailyCheckInController(DailyCheckInService checkInService) {
        this.checkInService = checkInService;
    }

    /**
     * POST /api/checkins/{userId}
     * Checka in idag med humörpoäng och valfri anteckning.
     * Uppdaterar befintlig incheckning om användaren redan checkat in idag.
     */
    @PostMapping("/{userId}")
    public CheckInResponseDto checkIn(
            @PathVariable String userId,
            @Valid @RequestBody CheckInRequestDto request) {
        verifyUserAccess(userId);
        return checkInService.checkIn(userId, request);
    }

    /**
     * GET /api/checkins/{userId}/today
     * Hämta dagens incheckning (om den finns).
     */
    @GetMapping("/{userId}/today")
    public ResponseEntity<CheckInResponseDto> getToday(@PathVariable String userId) {
        verifyUserAccess(userId);
        return checkInService.getTodaysCheckIn(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * GET /api/checkins/{userId}/history?days=30
     * Hämta incheckningshistorik för de senaste N dagarna (default 30).
     */
    @GetMapping("/{userId}/history")
    public List<CheckInResponseDto> getHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "30") int days) {
        verifyUserAccess(userId);
        if (days <= 0 || days > 365) {
            throw new IllegalArgumentException("Days must be between 1 and 365");
        }
        return checkInService.getHistory(userId, days);
    }
}
