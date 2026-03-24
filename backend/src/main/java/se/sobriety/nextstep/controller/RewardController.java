package se.sobriety.nextstep.controller;

import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.CollectibleDto;
import se.sobriety.nextstep.dto.DailyRewardDto;
import se.sobriety.nextstep.service.RewardService;

import java.util.List;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    /**
     * GET /api/rewards/{userId}/today
     * Hämta dagens belöning (null om ingen genererats)
     */
    @GetMapping("/{userId}/today")
    public DailyRewardDto getTodayReward(@PathVariable String userId) {
        verifyUserAccess(userId);
        return rewardService.getTodayReward(userId);
    }

    /**
     * POST /api/rewards/{userId}/generate
     * Generera dagens belöning (idempotent)
     */
    @PostMapping("/{userId}/generate")
    public DailyRewardDto generateReward(@PathVariable String userId) {
        verifyUserAccess(userId);
        return rewardService.generateDailyReward(userId);
    }

    /**
     * POST /api/rewards/{userId}/claim
     * Claima dagens belöning (få XP + spara collectible)
     */
    @PostMapping("/{userId}/claim")
    public DailyRewardDto claimReward(@PathVariable String userId) {
        verifyUserAccess(userId);
        return rewardService.claimReward(userId);
    }

    /**
     * GET /api/rewards/{userId}/collection
     * Hämta användarens samling
     */
    @GetMapping("/{userId}/collection")
    public List<CollectibleDto> getCollection(@PathVariable String userId) {
        verifyUserAccess(userId);
        return rewardService.getCollection(userId);
    }
}
