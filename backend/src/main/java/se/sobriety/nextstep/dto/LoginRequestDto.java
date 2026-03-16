package se.sobriety.nextstep.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO för login request
 */
public record LoginRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(max = 128, message = "Password must be at most 128 characters")
        String password
) {}
