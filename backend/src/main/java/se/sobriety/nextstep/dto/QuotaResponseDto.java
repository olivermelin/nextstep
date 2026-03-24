package se.sobriety.nextstep.dto;

/**
 * Svar från kvot-endpointen — visar hur många AI-meddelanden användaren har kvar idag.
 *
 * @param used         Antal meddelanden använda idag
 * @param remaining    Antal meddelanden kvar (Integer.MAX_VALUE för PREMIUM)
 * @param dailyLimit   Daglig gräns (Integer.MAX_VALUE för PREMIUM = obegränsat)
 * @param isPremium    Sant om användaren har premium-prenumeration
 * @param resetAt      Tidpunkt (ISO-datum) då kvoten nollställs (nästa midnatt)
 */
public record QuotaResponseDto(
        int used,
        int remaining,
        int dailyLimit,
        boolean isPremium,
        String resetAt
) {}
