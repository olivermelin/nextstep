package se.sobriety.nextstep.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.ChallengeInDto;
import se.sobriety.nextstep.dto.ChallengeOutDto;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.mapper.ChallengeMapper;
import se.sobriety.nextstep.repository.ChallengeRepository;
import se.sobriety.nextstep.repository.UserChallengeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMapper challengeMapper;
    private final UserChallengeRepository userChallengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository,
                            ChallengeMapper challengeMapper,
                            UserChallengeRepository userChallengeRepository) {
        this.challengeRepository = challengeRepository;
        this.challengeMapper = challengeMapper;
        this.userChallengeRepository = userChallengeRepository;
    }

    public List<ChallengeOutDto> getAllChallenges() {
        return challengeRepository.findAll()
                .stream()
                .map(challengeMapper::toDto)
                .collect(Collectors.toList());
    }

    public ChallengeOutDto getChallengeById(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found with id: " + id));
        return challengeMapper.toDto(challenge);
    }

    public List<ChallengeOutDto> getChallengesByCategory(ChallengeCategory category) {
        return challengeRepository.findByCategory(category)
                .stream()
                .map(challengeMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ChallengeOutDto> getChallengesByDifficulty(ChallengeDifficulty difficulty) {
        return challengeRepository.findByDifficulty(difficulty)
                .stream()
                .map(challengeMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ChallengeOutDto> getChallengesByCategoryAndDifficulty(
            ChallengeCategory category, ChallengeDifficulty difficulty) {
        return challengeRepository.findByCategoryAndDifficulty(category, difficulty)
                .stream()
                .map(challengeMapper::toDto)
                .collect(Collectors.toList());
    }

    // ============ Metoder med completedToday-status per användare ============

    private Set<Long> getCompletedTodayIds(String userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return new HashSet<>(userChallengeRepository.findCompletedChallengeIdsSince(userId, startOfDay));
    }

    /**
     * Mappa challenges med completedToday-status för en användare.
     */
    private List<ChallengeOutDto> mapWithCompletionStatus(List<Challenge> challenges, String userId) {
        Set<Long> completedTodayIds = getCompletedTodayIds(userId);
        return challenges.stream()
                .map(c -> challengeMapper.toDto(c, completedTodayIds.contains(c.getId())))
                .collect(Collectors.toList());
    }

    public List<ChallengeOutDto> getAllChallengesForUser(String userId) {
        return mapWithCompletionStatus(challengeRepository.findAll(), userId);
    }

    public List<ChallengeOutDto> getChallengesByCategoryForUser(ChallengeCategory category, String userId) {
        return mapWithCompletionStatus(challengeRepository.findByCategory(category), userId);
    }

    public List<ChallengeOutDto> getChallengesByDifficultyForUser(ChallengeDifficulty difficulty, String userId) {
        return mapWithCompletionStatus(challengeRepository.findByDifficulty(difficulty), userId);
    }

    public List<ChallengeOutDto> getChallengesByCategoryAndDifficultyForUser(
            ChallengeCategory category, ChallengeDifficulty difficulty, String userId) {
        return mapWithCompletionStatus(challengeRepository.findByCategoryAndDifficulty(category, difficulty), userId);
    }

    // ============ CRUD – Skapa, uppdatera och ta bort challenges dynamiskt ============

    /**
     * Skapa en ny challenge dynamiskt via API.
     */
    @Transactional
    public ChallengeOutDto createChallenge(ChallengeInDto dto) {
        ChallengeDifficulty difficulty = ChallengeDifficulty.valueOf(dto.difficulty());
        ChallengeCategory category = ChallengeCategory.valueOf(dto.category());

        Challenge challenge = new Challenge(
                dto.title(),
                dto.description(),
                dto.durationMinutes(),
                difficulty,
                category
        );
        challenge.setYoutubeUrl(dto.youtubeUrl());
        challenge.setInstructions(dto.instructions());

        challenge = challengeRepository.save(challenge);
        return challengeMapper.toDto(challenge);
    }

    /**
     * Uppdatera en befintlig challenge.
     */
    @Transactional
    public ChallengeOutDto updateChallenge(Long id, ChallengeInDto dto) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found with id: " + id));

        challenge.setTitle(dto.title());
        challenge.setDescription(dto.description());
        challenge.setDurationMinutes(dto.durationMinutes());
        challenge.setDifficulty(ChallengeDifficulty.valueOf(dto.difficulty()));
        challenge.setCategory(ChallengeCategory.valueOf(dto.category()));
        challenge.setYoutubeUrl(dto.youtubeUrl());
        challenge.setInstructions(dto.instructions());

        challenge = challengeRepository.save(challenge);
        return challengeMapper.toDto(challenge);
    }

    /**
     * Ta bort en challenge.
     */
    @Transactional
    public void deleteChallenge(Long id) {
        if (!challengeRepository.existsById(id)) {
            throw new IllegalArgumentException("Challenge not found with id: " + id);
        }
        challengeRepository.deleteById(id);
    }
}

