package se.sobriety.nextstep.dto;

import java.util.List;

/**
 * DTO för kategori-framsteg response
 */
public record CategoryProgressResponseDto(
        String userId,
        int days,
        List<CategoryProgressDto> categories
) {}

