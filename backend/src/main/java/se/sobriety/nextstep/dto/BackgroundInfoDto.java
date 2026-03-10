package se.sobriety.nextstep.dto;

import se.sobriety.nextstep.entity.CurrentSituation;
import se.sobriety.nextstep.entity.Gender;
import se.sobriety.nextstep.entity.SubstanceType;

import java.util.List;

/**
 * DTO för bakgrundsinformation
 */
public record BackgroundInfoDto(
        Integer age,
        Gender gender,
        CurrentSituation currentSituation,
        List<SubstanceType> substanceHistory
) {}

