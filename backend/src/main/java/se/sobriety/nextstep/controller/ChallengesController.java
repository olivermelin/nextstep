package se.sobriety.nextstep.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import se.sobriety.nextstep.dto.ChallengeInDto;
import se.sobriety.nextstep.dto.ChallengeOutDto;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.service.ChallengeService;

import java.util.List;

import static se.sobriety.nextstep.util.SecurityUtils.verifyUserAccess;

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

    /* ============ ENDPOINTS MED COMPLETEDTODAY-STATUS PER ANVÄNDARE ============ */

    /**
     * GET /api/challenges/user/{userId} - Hämta alla challenges med completedToday-status.
     * Returnerar alla challenges med fältet completedToday = true/false.
     */
    @GetMapping("/user/{userId:.+}")
    public List<ChallengeOutDto> getAllChallengesForUser(@PathVariable String userId) {
        verifyUserAccess(userId);
        return challengeService.getAllChallengesForUser(userId);
    }

    /**
     * GET /api/challenges/user/{userId}/category/{category}
     */
    @GetMapping("/user/{userId:.+}/category/{category}")
    public List<ChallengeOutDto> getChallengesByCategoryForUser(
            @PathVariable String userId,
            @PathVariable ChallengeCategory category) {
        verifyUserAccess(userId);
        return challengeService.getChallengesByCategoryForUser(category, userId);
    }

    /**
     * GET /api/challenges/user/{userId}/difficulty/{difficulty}
     */
    @GetMapping("/user/{userId:.+}/difficulty/{difficulty}")
    public List<ChallengeOutDto> getChallengesByDifficultyForUser(
            @PathVariable String userId,
            @PathVariable ChallengeDifficulty difficulty) {
        verifyUserAccess(userId);
        return challengeService.getChallengesByDifficultyForUser(difficulty, userId);
    }

    /**
     * GET /api/challenges/user/{userId}/category/{category}/difficulty/{difficulty}
     */
    @GetMapping("/user/{userId:.+}/category/{category}/difficulty/{difficulty}")
    public List<ChallengeOutDto> getChallengesByCategoryAndDifficultyForUser(
            @PathVariable String userId,
            @PathVariable ChallengeCategory category,
            @PathVariable ChallengeDifficulty difficulty) {
        verifyUserAccess(userId);
        return challengeService.getChallengesByCategoryAndDifficultyForUser(category, difficulty, userId);
    }

    /* ============ CRUD – Skapa, uppdatera och ta bort challenges ============ */

    /**
     * POST /api/challenges - Skapa en ny challenge dynamiskt
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeOutDto createChallenge(@Valid @RequestBody ChallengeInDto challengeInDto) {
        return challengeService.createChallenge(challengeInDto);
    }

    /**
     * PUT /api/challenges/{id} - Uppdatera en befintlig challenge
     */
    @PutMapping("/{id}")
    public ChallengeOutDto updateChallenge(@PathVariable Long id, @Valid @RequestBody ChallengeInDto challengeInDto) {
        return challengeService.updateChallenge(id, challengeInDto);
    }

    /**
     * DELETE /api/challenges/{id} - Ta bort en challenge
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChallenge(@PathVariable Long id) {
        challengeService.deleteChallenge(id);
    }
}
