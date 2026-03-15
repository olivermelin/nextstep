package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import se.sobriety.nextstep.entity.CurrentSituation;
import se.sobriety.nextstep.entity.Gender;
import se.sobriety.nextstep.entity.SubstanceType;

import java.util.List;

/**
 * DTO för bakgrundsinformation
 */
public record BackgroundInfoDto(
        @Min(value = 13, message = "Age must be at least 13")
        @Max(value = 120, message = "Age must be at most 120")
        Integer age,

        Gender gender,

        CurrentSituation currentSituation,

        List<SubstanceType> substanceHistory
) {}
