package se.sobriety.nextstep.controller;

import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.ChallengeOutDto;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.service.ChallengeService;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class ChallengesController {

    private final ChallengeService challengeService;

    public ChallengesController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    /* ===================== CHALLENGE ENDPOINTS ===================== */

    /**
     * GET /api/challenges - Hämta alla challenges
     */
    @GetMapping
    public List<ChallengeOutDto> getAllChallenges() {
        return challengeService.getAllChallenges();
    }

    /**
     * GET /api/challenges/{id} - Hämta specifik challenge
     */
    @GetMapping("/{id}")
    public ChallengeOutDto getChallengeById(@PathVariable Long id) {
        return challengeService.getChallengeById(id);
    }

    /**
     * GET /api/challenges/category/{category} - Hämta challenges per kategori
     */
    @GetMapping("/category/{category}")
    public List<ChallengeOutDto> getChallengesByCategory(@PathVariable ChallengeCategory category) {
        return challengeService.getChallengesByCategory(category);
    }

    /**
     * GET /api/challenges/difficulty/{difficulty} - Hämta challenges per svårighetsgrad
     */
    @GetMapping("/difficulty/{difficulty}")
    public List<ChallengeOutDto> getChallengesByDifficulty(@PathVariable ChallengeDifficulty difficulty) {
        return challengeService.getChallengesByDifficulty(difficulty);
    }

    /**
     * GET /api/challenges/category/{category}/difficulty/{difficulty}
     */
    @GetMapping("/category/{category}/difficulty/{difficulty}")
    public List<ChallengeOutDto> getChallengesByCategoryAndDifficulty(
            @PathVariable ChallengeCategory category,
            @PathVariable ChallengeDifficulty difficulty) {
        return challengeService.getChallengesByCategoryAndDifficulty(category, difficulty);
    }
}
