package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendFriendRequestDto(
        @NotBlank @Email String targetEmail
) {}
