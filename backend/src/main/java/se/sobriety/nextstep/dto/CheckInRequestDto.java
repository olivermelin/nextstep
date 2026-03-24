package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request-DTO för daglig incheckning
 */
public record CheckInRequestDto(
        @NotNull(message = "Mood score är obligatoriskt")
        @Min(value = 1, message = "Mood score måste vara minst 1")
        @Max(value = 5, message = "Mood score får vara max 5")
        Integer moodScore,

        @Size(max = 500, message = "Anteckning får max vara 500 tecken")
        String note
) {}
