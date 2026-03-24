package se.sobriety.nextstep.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sobriety.nextstep.dto.UserChallengeOutDto;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.UserChallenge;
import se.sobriety.nextstep.mapper.UserChallengeMapper;
import se.sobriety.nextstep.repository.ChallengeRepository;
import se.sobriety.nextstep.repository.UserChallengeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserChallengeService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeMapper userChallengeMapper;
    private final UserProgressService userProgressService;
    private final StreakService streakService;

    public UserChallengeService(
            UserChallengeRepository userChallengeRepository,
            ChallengeRepository challengeRepository,
            UserChallengeMapper userChallengeMapper,
            UserProgressService userProgressService,
            StreakService streakService) {
        this.userChallengeRepository = userChallengeRepository;
        this.challengeRepository = challengeRepository;
        this.userChallengeMapper = userChallengeMapper;
        this.userProgressService = userProgressService;
        this.streakService = streakService;
    }

    /**
     * Starta en challenge för en användare.
     * Blockerar om samma challenge redan har slutförts idag.
     * Returnerar befintlig aktiv challenge om den redan är startad.
     * Tillåter att göra om en challenge nästa dag.
     */
    public UserChallengeOutDto startChallenge(String userId, Long challengeId) {
        // Validera att användaren inte redan har slutfört denna challenge idag
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean alreadyCompletedToday = userChallengeRepository
                .existsCompletedSince(userId, challengeId, startOfDay);

        if (alreadyCompletedToday) {
            throw new IllegalArgumentException("Challenge already completed today. Try again tomorrow!");
        }

        // Returnera befintlig aktiv (ej slutförd) challenge om den finns (samma challenge)
        var existing = userChallengeRepository
                .findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(userId, challengeId, false);
        if (existing.isPresent()) {
            return userChallengeMapper.toDto(existing.get());
        }

        // Blockera om en ANNAN challenge redan pågår idag
        List<UserChallenge> activeOther = userChallengeRepository.findByUserIdAndCompleted(userId, false)
                .stream()
                .filter(uc -> uc.getStartedAt().isAfter(startOfDay))
                .filter(uc -> !uc.getChallenge().getId().equals(challengeId))
                .toList();
        if (!activeOther.isEmpty()) {
            String activeName = activeOther.getFirst().getChallenge().getTitle();
            throw new IllegalArgumentException(
                    "Du har redan en pågående aktivitet: \"" + activeName + "\". Slutför den först!");
        }

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        UserChallenge userChallenge = new UserChallenge(userId, challenge);
        userChallenge = userChallengeRepository.save(userChallenge);

        return userChallengeMapper.toDto(userChallenge);
    }

    /**
     * Slutför en challenge och tilldela poäng.
     * Blockerar om samma challenge redan slutförts idag.
     *
     * @param actualMinutes faktisk tid som användaren spenderade (0 = använd standardtid för utmaningen)
     */
    public UserChallengeOutDto completeChallenge(String userId, Long challengeId, int actualMinutes) {
        // Kolla om redan slutförd idag
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean alreadyCompletedToday = userChallengeRepository
                .existsCompletedSince(userId, challengeId, startOfDay);

        if (alreadyCompletedToday) {
            throw new IllegalArgumentException("Challenge already completed today. Try again tomorrow!");
        }

        // Hitta aktiv (ej slutförd) challenge
        UserChallenge userChallenge = userChallengeRepository
                .findTopByUserIdAndChallengeIdAndCompletedOrderByStartedAtDesc(userId, challengeId, false)
                .orElseThrow(() -> new IllegalArgumentException("User challenge not found"));

        // Markera som slutförd och tilldela poäng (tidsskalat)
        userChallenge.complete(actualMinutes);
        userChallenge = userChallengeRepository.save(userChallenge);

        // Lägg till poäng i UserProgress
        int points = userChallenge.getPointsEarned();
        userProgressService.addPoints(userId, points);

        // Uppdatera streak — registrera dagens aktivitet
        streakService.registerActivity(userId);

        return userChallengeMapper.toDto(userChallenge);
    }

    /**
     * Hämta alla challenges för en användare (aktiva idag + slutförda)
     */
    @Transactional(readOnly = true)
    public List<UserChallengeOutDto> getUserChallenges(String userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return userChallengeRepository.findByUserId(userId)
                .stream()
                .filter(uc -> uc.isCompleted() || uc.getStartedAt().isAfter(startOfDay))
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
     * Hämta pågående challenges för en användare (startade idag)
     */
    @Transactional(readOnly = true)
    public List<UserChallengeOutDto> getUserActiveChallenges(String userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return userChallengeRepository.findByUserIdAndCompleted(userId, false)
                .stream()
                .filter(uc -> uc.getStartedAt().isAfter(startOfDay))
                .map(userChallengeMapper::toDto)
                .collect(Collectors.toList());
    }
}

