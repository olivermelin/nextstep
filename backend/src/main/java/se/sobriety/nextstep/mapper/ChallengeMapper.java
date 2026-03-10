package se.sobriety.nextstep.mapper;

import org.springframework.stereotype.Component;
import se.sobriety.nextstep.dto.ChallengeOutDto;
import se.sobriety.nextstep.entity.Challenge;

@Component
public class ChallengeMapper {

    public ChallengeOutDto toDto(Challenge entity) {
        return new ChallengeOutDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDurationMinutes(),
                entity.getDifficulty().name(),
                entity.getCategory().name(),
                entity.getYoutubeUrl(),
                entity.getInstructions(),
                entity.getCreatedAt()
        );
    }
}

