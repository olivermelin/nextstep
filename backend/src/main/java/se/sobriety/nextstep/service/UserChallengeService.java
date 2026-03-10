package se.sobriety.nextstep.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.UserChallengeOutDto;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.UserChallenge;
import se.sobriety.nextstep.mapper.UserChallengeMapper;
import se.sobriety.nextstep.repository.ChallengeRepository;
import se.sobriety.nextstep.repository.UserChallengeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserChallengeService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeMapper userChallengeMapper;
    private final UserProgressService userProgressService;

    public UserChallengeService(
            UserChallengeRepository userChallengeRepository,
            ChallengeRepository challengeRepository,
            UserChallengeMapper userChallengeMapper,
            UserProgressService userProgressService) {
        this.userChallengeRepository = userChallengeRepository;
        this.challengeRepository = challengeRepository;
        this.userChallengeMapper = userChallengeMapper;
        this.userProgressService = userProgressService;
    }

    /**
     * Starta en challenge för en användare
     */
    public UserChallengeOutDto startChallenge(String userId, Long challengeId) {
        // Validera att användaren inte redan har slutfört denna challenge
        boolean alreadyCompleted = userChallengeRepository
                .existsByUserIdAndChallengeIdAndCompleted(userId, challengeId, true);

        if (alreadyCompleted) {
            throw new IllegalArgumentException("Challenge already completed by user");
        }

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        UserChallenge userChallenge = new UserChallenge(userId, challenge);
        userChallenge = userChallengeRepository.save(userChallenge);

        return userChallengeMapper.toDto(userChallenge);
    }

    /**
     * Slutför en challenge och tilldela poäng
     */
    public UserChallengeOutDto completeChallenge(String userId, Long challengeId) {
        UserChallenge userChallenge = userChallengeRepository
                .findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("User challenge not found"));

        if (userChallenge.isCompleted()) {
            throw new IllegalArgumentException("Challenge already completed");
        }

        // Markera som slutförd och tilldela poäng
        userChallenge.complete();
        userChallenge = userChallengeRepository.save(userChallenge);

        // Lägg till poäng i UserProgress
        int points = userChallenge.getPointsEarned();
        userProgressService.addPoints(userId, points);

        return userChallengeMapper.toDto(userChallenge);
    }

    /**
     * Hämta alla challenges för en användare
     */
    @Transactional(readOnly = true)
    public List<UserChallengeOutDto> getUserChallenges(String userId) {
        return userChallengeRepository.findByUserId(userId)
                .stream()
                .map(userChallengeMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Hämta slutförda challenges för en användare
     */
    @Transactional(readOnly = true)
    public List<UserChallengeOutDto> getUserCompletedChallenges(String userId) {
        return userChallengeRepository.findByUserIdAndCompleted(userId, true)
                .stream()
                .map(userChallengeMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Hämta pågående challenges för en användare
     */
    @Transactional(readOnly = true)
    public List<UserChallengeOutDto> getUserActiveChallenges(String userId) {
        return userChallengeRepository.findByUserIdAndCompleted(userId, false)
                .stream()
                .map(userChallengeMapper::toDto)
                .collect(Collectors.toList());
    }
}

