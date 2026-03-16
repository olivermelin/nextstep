package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.OnboardingDataDto;
import se.sobriety.nextstep.dto.OnboardingResponseDto;
import se.sobriety.nextstep.dto.OnboardingStatusDto;
import se.sobriety.nextstep.service.OnboardingService;

/**
 * Controller för onboarding-endpoints
 */
import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * POST /api/onboarding/complete/{userId}
     * Slutför onboarding och spara användardata
     */
    @PostMapping("/complete/{userId}")
    public ResponseEntity<OnboardingResponseDto> completeOnboarding(
            @PathVariable String userId,
            @Valid @RequestBody OnboardingDataDto data) {

        verifyUserAccess(userId);
        onboardingService.completeOnboarding(userId, data);

        return ResponseEntity.ok(
                new OnboardingResponseDto(true, "Onboarding completed successfully")
        );
    }

    /**
     * GET /api/onboarding/status/{userId}
     * Kontrollera om användaren har slutfört onboarding
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<OnboardingStatusDto> getOnboardingStatus(@PathVariable String userId) {
        verifyUserAccess(userId);
        boolean completed = onboardingService.isOnboardingCompleted(userId);
        return ResponseEntity.ok(new OnboardingStatusDto(completed));
    }
}

