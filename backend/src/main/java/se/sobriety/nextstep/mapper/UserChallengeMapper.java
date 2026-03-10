package se.sobriety.nextstep.mapper;

import org.springframework.stereotype.Component;
import se.sobriety.nextstep.dto.UserChallengeOutDto;
import se.sobriety.nextstep.entity.UserChallenge;

@Component
public class UserChallengeMapper {

    private final ChallengeMapper challengeMapper;

    public UserChallengeMapper(ChallengeMapper challengeMapper) {
        this.challengeMapper = challengeMapper;
    }

    public UserChallengeOutDto toDto(UserChallenge entity) {
        return new UserChallengeOutDto(
                entity.getId(),
                entity.getUserId(),
                challengeMapper.toDto(entity.getChallenge()),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.isCompleted(),
                entity.getPointsEarned()
        );
    }
}

