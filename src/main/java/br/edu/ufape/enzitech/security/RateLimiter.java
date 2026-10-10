package br.edu.ufape.enzitech.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    public static final String MESSAGE = "Muitas tentativas. Aguarde alguns minutos e tente novamente.";
    private static final int EVICTION_THRESHOLD = 10_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter() {
        this(Clock.systemUTC());
    }

    RateLimiter(Clock clock) {
        this.clock = clock;
    }

    public void check(String key, int maxAttempts, Duration window) {
        Instant now = clock.instant();
        Window current = windows.compute(key, (k, existing) ->
                existing == null || !now.isBefore(existing.resetAt())
                        ? new Window(now.plus(window), 1)
                        : new Window(existing.resetAt(), existing.count() + 1));

        if (windows.size() > EVICTION_THRESHOLD) {
            windows.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().resetAt()));
        }

        if (current.count() > maxAttempts) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, MESSAGE);
        }
    }

    public void reset(String key) {
        windows.remove(key);
    }

    private record Window(Instant resetAt, int count) {
    }
}
