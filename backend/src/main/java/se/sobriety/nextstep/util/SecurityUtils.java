package se.sobriety.nextstep.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import se.sobriety.nextstep.exception.UnauthorizedAccessException;

/**
 * Utility class for extracting and verifying the authenticated user
 * from the Spring Security context. Stödjer både OAuth2 och email/password.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns the authenticated user's ID (email) from the security context.
     * Stödjer OAuth2AuthenticationToken och UsernamePasswordAuthenticationToken.
     *
     * @return the current user's ID (email), or null if not authenticated
     */
    public static String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            return (String) oauthToken.getPrincipal().getAttribute("email");
        }
        if (auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
            return auth.getName(); // email används som username
        }
        return null;
    }

    /**
     * Verifies that the authenticated user matches the requested userId.
     * Throws UnauthorizedAccessException (403) if there is a mismatch.
     *
     * @param requestedUserId the userId from the request path or body
     * @throws UnauthorizedAccessException if the authenticated user does not match
     */
    public static void verifyUserAccess(String requestedUserId) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(requestedUserId)) {
            throw new UnauthorizedAccessException(
                    "Access denied: you can only access your own data"
            );
        }
    }
}
