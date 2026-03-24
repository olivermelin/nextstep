package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDuelDto(
        @NotBlank @Email String challengedEmail,
        @NotNull Long challengeId,
        @NotBlank String challengeName
) {}
