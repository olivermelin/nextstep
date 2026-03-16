package se.sobriety.nextstep.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new RateLimitingFilter();
    }

    // --- Requests within rate limit pass through ---

    @Test
    void doFilter_generalApiWithinLimit_passesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/some-endpoint");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilter_coachEndpointWithinLimit_passesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/coach/chat");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_authEndpointWithinLimit_passesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.2");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    // --- Non-API paths bypass rate limiting ---

    @Test
    void doFilter_nonApiPath_bypassesRateLimiting() throws Exception {
        when(request.getRequestURI()).thenReturn("/health");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    void doFilter_staticResourcePath_bypassesRateLimiting() throws Exception {
        when(request.getRequestURI()).thenReturn("/index.html");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    // --- Requests exceeding rate limit get 429 ---

    @Test
    void doFilter_authEndpointExceedsLimit_returns429() throws Exception {
        String ip = "10.0.0.10";
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn(ip);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Auth limit is 10 per minute
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }
        verify(chain, times(10)).doFilter(request, response);

        // 11th request should be rejected
        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setContentType("application/json");
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Too many requests"));
    }

    @Test
    void doFilter_coachEndpointExceedsLimit_returns429() throws Exception {
        String ip = "10.0.0.20";
        when(request.getRequestURI()).thenReturn("/api/coach/chat");
        when(request.getRemoteAddr()).thenReturn(ip);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Coach limit is 20 per minute
        for (int i = 0; i < 20; i++) {
            filter.doFilter(request, response, chain);
        }
        verify(chain, times(20)).doFilter(request, response);

        // 21st request should be rejected
        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Rate limit exceeded"));
    }

    @Test
    void doFilter_generalApiExceedsLimit_returns429() throws Exception {
        String ip = "10.0.0.30";
        when(request.getRequestURI()).thenReturn("/api/data");
        when(request.getRemoteAddr()).thenReturn(ip);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // General limit is 100 per minute
        for (int i = 0; i < 100; i++) {
            filter.doFilter(request, response, chain);
        }
        verify(chain, times(100)).doFilter(request, response);

        // 101st request should be rejected
        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    // --- Different IPs have separate rate limits ---

    @Test
    void doFilter_differentIps_haveSeparateLimits() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Exhaust limit for IP-A
        when(request.getRemoteAddr()).thenReturn("1.1.1.1");
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }

        // IP-A should now be blocked
        filter.doFilter(request, response, chain);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        // IP-B should still be allowed
        when(request.getRemoteAddr()).thenReturn("2.2.2.2");
        filter.doFilter(request, response, chain);
        // 10 for IP-A + 1 for IP-B = 11 successful calls
        verify(chain, times(11)).doFilter(request, response);
    }

    @Test
    void doFilter_sameIpDifferentEndpointTypes_haveSeparateBuckets() throws Exception {
        String ip = "10.0.0.50";
        when(request.getRemoteAddr()).thenReturn(ip);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Exhaust auth limit (10 requests)
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }

        // Auth should be blocked now
        filter.doFilter(request, response, chain);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        // But coach endpoint for same IP should still work
        reset(response);
        when(request.getRequestURI()).thenReturn("/api/coach/chat");
        filter.doFilter(request, response, chain);
        verify(response, never()).setStatus(anyInt());
    }

    // --- X-Forwarded-For header handling ---

    @Test
    void doFilter_xForwardedForHeader_usesFirstIp() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5, 70.41.3.18, 150.172.238.178");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Exhaust limit using X-Forwarded-For IP
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }

        // Should be rate limited based on X-Forwarded-For IP
        filter.doFilter(request, response, chain);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void doFilter_noXForwardedForHeader_usesRemoteAddr() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_emptyXForwardedForHeader_usesRemoteAddr() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("192.168.1.101");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        // Verify it fell through to getRemoteAddr
        verify(request).getRemoteAddr();
    }

    // --- Edge cases ---

    @Test
    void doFilter_exactlyAtLimit_stillAllowed() throws Exception {
        String ip = "10.0.0.60";
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(request.getRemoteAddr()).thenReturn(ip);

        // Use all 10 tokens -- all should succeed
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }

        verify(chain, times(10)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilter_responseBodyContainsExpectedJson() throws Exception {
        String ip = "10.0.0.70";
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn(ip);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Exhaust limit
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }

        // Trigger rate limit
        filter.doFilter(request, response, chain);
        printWriter.flush();

        String body = stringWriter.toString();
        assertTrue(body.contains("\"error\":\"Too many requests\""));
        assertTrue(body.contains("\"message\":\"Rate limit exceeded. Please try again later.\""));
    }

    @Test
    void doFilter_pathExactlyApi_isRateLimited() throws Exception {
        // "/api/" itself should go through general rate limiting
        when(request.getRequestURI()).thenReturn("/api/");
        when(request.getRemoteAddr()).thenReturn("10.0.0.80");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_chainNotCalledWhenRateLimited() throws Exception {
        String ip = "10.0.0.90";
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn(ip);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // Exhaust limit
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }

        // Reset chain to track only subsequent calls
        reset(chain);

        // This request should NOT pass through the chain
        filter.doFilter(request, response, chain);
        verify(chain, never()).doFilter(request, response);
    }
}
