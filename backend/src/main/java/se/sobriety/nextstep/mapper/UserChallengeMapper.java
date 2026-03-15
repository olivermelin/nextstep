package se.sobriety.nextstep.mapper;

import org.springframework.stereotype.Component;
import se.sobriety.nextstep.dto.UserChallengeOutDto;
import se.sobriety.nextstep.entity.Challenge;
import se.sobriety.nextstep.entity.UserChallenge;

@Component
public class UserChallengeMapper {

    public UserChallengeOutDto toDto(UserChallenge entity) {
        Challenge challenge = entity.getChallenge();
        String status = entity.isCompleted() ? "COMPLETED" : "IN_PROGRESS";

        return new UserChallengeOutDto(
                entity.getId(),
                challenge.getId(),
                challenge.getTitle(),
                challenge.getCategory().name(),
                challenge.getDifficulty().name(),
                challenge.getDurationMinutes(),
                status,
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getPointsEarned()
        );
    }
}

