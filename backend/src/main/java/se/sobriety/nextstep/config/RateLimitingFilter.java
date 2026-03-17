package se.sobriety.nextstep.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rate limiting filter for API endpoints.
 * - AI coach endpoints: 20 requests per minute per IP
 * - Auth endpoints: 10 requests per minute per IP
 * - General API: 100 requests per minute per IP
 *
 * Bucket-maps har en maxstorlek på 10 000 entries per typ
 * för att förhindra minnesläckor vid DDoS.
 */
@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private static final int MAX_BUCKETS_PER_TYPE = 10_000;

    private final Map<String, Bucket> coachBuckets = createBoundedMap(MAX_BUCKETS_PER_TYPE);
    private final Map<String, Bucket> authBuckets = createBoundedMap(MAX_BUCKETS_PER_TYPE);
    private final Map<String, Bucket> generalBuckets = createBoundedMap(MAX_BUCKETS_PER_TYPE);

    /**
     * Skapar en thread-safe LRU-cache med maxstorlek.
     * Äldsta entries tas bort automatiskt när gränsen nås.
     */
    private static Map<String, Bucket> createBoundedMap(int maxSize) {
        return Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Bucket> eldest) {
                return size() > maxSize;
            }
        });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(httpRequest);
        Bucket bucket = resolveBucket(clientIp, path);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                    "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}"
            );
        }
    }

    private Bucket resolveBucket(String clientIp, String path) {
        if (path.startsWith("/api/coach/")) {
            return coachBuckets.computeIfAbsent(clientIp, k -> createBucket(20, Duration.ofMinutes(1)));
        } else if (path.startsWith("/api/auth/")) {
            return authBuckets.computeIfAbsent(clientIp, k -> createBucket(10, Duration.ofMinutes(1)));
        } else {
            return generalBuckets.computeIfAbsent(clientIp, k -> createBucket(100, Duration.ofMinutes(1)));
        }
    }

    private Bucket createBucket(int capacity, Duration refillDuration) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillDuration)
                        .build())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
