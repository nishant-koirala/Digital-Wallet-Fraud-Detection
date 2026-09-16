package dev.nishanta.wallet.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> transferBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        if (path.contains("/api/v1/auth/login")) {
            Bucket bucket = loginBuckets.computeIfAbsent(ip, this::createNewLoginBucket);
            if (bucket.tryConsume(1)) {
                return true;
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many login attempts. Please try again later.");
                return false;
            }
        }

        if (path.contains("/api/v1/transactions/transfer")) {
            Bucket bucket = transferBuckets.computeIfAbsent(ip, this::createNewTransferBucket);
            if (bucket.tryConsume(1)) {
                return true;
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many transfer attempts. Please try again later.");
                return false;
            }
        }

        return true;
    }

    private Bucket createNewLoginBucket(String ip) {
        // 5 requests per minute
        Bandwidth limit = Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createNewTransferBucket(String ip) {
        // 10 requests per minute
        Bandwidth limit = Bandwidth.builder().capacity(10).refillIntervally(10, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }
}
