package se.sobriety.nextstep.exception;

/**
 * Kastas när en FREE-användare har nått sin dagliga gräns för AI-meddelanden.
 */
public class QuotaExceededException extends RuntimeException {
    private final int used;
    private final int limit;

    public QuotaExceededException(int used, int limit) {
        super("Daily message quota exceeded: " + used + "/" + limit);
        this.used = used;
        this.limit = limit;
    }

    public int getUsed() { return used; }
    public int getLimit() { return limit; }
}
