package se.sobriety.nextstep.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.ChallengeOutDto;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.ChallengeCategory;
import se.sobriety.nextstep.entity.ChallengeDifficulty;
import se.sobriety.nextstep.mapper.ChallengeMapper;
import se.sobriety.nextstep.repository.ChallengeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMapper challengeMapper;

    public ChallengeService(ChallengeRepository challengeRepository, ChallengeMapper challengeMapper) {
        this.challengeRepository = challengeRepository;
        this.challengeMapper = challengeMapper;
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
}

