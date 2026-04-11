package se.sobriety.nextstep.controller;

import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.UserChallengeOutDto;
import se.sobriety.nextstep.service.UserChallengeService;

import java.util.List;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/user-challenges")
public class UserChallengesController {

    private final UserChallengeService userChallengeService;

    public UserChallengesController(UserChallengeService userChallengeService) {
        this.userChallengeService = userChallengeService;
    }

    /**
     * GET /api/user-challenges/user/{userId} - Hämta alla användarens challenges
     */
    @GetMapping("/user/{userId:.+}")
    public List<UserChallengeOutDto> getUserChallenges(@PathVariable String userId) {
        verifyUserAccess(userId);
        return userChallengeService.getUserChallenges(userId);
    }

    /**
     * GET /api/user-challenges/user/{userId}/completed - Hämta slutförda challenges
     */
    @GetMapping("/user/{userId:.+}/completed")
    public List<UserChallengeOutDto> getUserCompletedChallenges(@PathVariable String userId) {
        verifyUserAccess(userId);
        return userChallengeService.getUserCompletedChallenges(userId);
    }

    /**
     * GET /api/user-challenges/user/{userId}/active - Hämta pågående challenges
     */
    @GetMapping("/user/{userId:.+}/active")
    public List<UserChallengeOutDto> getUserActiveChallenges(@PathVariable String userId) {
        verifyUserAccess(userId);
        return userChallengeService.getUserActiveChallenges(userId);
    }

    /**
     * POST /api/user-challenges/user/{userId}/challenge/{challengeId}/start - Starta en challenge
     */
    @PostMapping("/user/{userId:.+}/challenge/{challengeId}/start")
    public UserChallengeOutDto startChallenge(
            @PathVariable String userId,
            @PathVariable Long challengeId) {
        verifyUserAccess(userId);
        return userChallengeService.startChallenge(userId, challengeId);
    }

    /**
     * POST /api/user-challenges/user/{userId}/complete/{challengeId} - Slutför en challenge
     *
     * @param actualMinutes faktisk tid i minuter (valfri, 0 = använd standardtid för utmaningen)
     */
    @PostMapping("/user/{userId:.+}/complete/{challengeId}")
    public UserChallengeOutDto completeChallenge(
            @PathVariable String userId,
            @PathVariable Long challengeId,
            @RequestParam(defaultValue = "0") int actualMinutes,
            @RequestParam(defaultValue = "false") boolean shareToFeed) {
        verifyUserAccess(userId);
        return userChallengeService.completeChallenge(userId, challengeId, actualMinutes, shareToFeed);
    }
}

