package br.edu.ufape.enzitech.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimiterTest {

    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final MutableClock clock = new MutableClock(Instant.parse("2026-10-10T12:00:00Z"));
    private final RateLimiter rateLimiter = new RateLimiter(clock);

    @Test
    void allowsAttemptsUpToTheLimit() {
        assertThatCode(() -> {
            for (int i = 0; i < 3; i++) rateLimiter.check("k", 3, WINDOW);
        }).doesNotThrowAnyException();
    }

    @Test
    void blocksWhenLimitIsExceeded() {
        for (int i = 0; i < 3; i++) rateLimiter.check("k", 3, WINDOW);

        assertThatThrownBy(() -> rateLimiter.check("k", 3, WINDOW))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void keysAreIndependent() {
        for (int i = 0; i < 3; i++) rateLimiter.check("a", 3, WINDOW);

        assertThatCode(() -> rateLimiter.check("b", 3, WINDOW)).doesNotThrowAnyException();
    }

    @Test
    void windowResetsAfterItExpires() {
        for (int i = 0; i < 3; i++) rateLimiter.check("k", 3, WINDOW);

        clock.advance(WINDOW);

        assertThatCode(() -> rateLimiter.check("k", 3, WINDOW)).doesNotThrowAnyException();
    }

    @Test
    void resetClearsTheCounter() {
        for (int i = 0; i < 3; i++) rateLimiter.check("k", 3, WINDOW);

        rateLimiter.reset("k");

        assertThatCode(() -> rateLimiter.check("k", 3, WINDOW)).doesNotThrowAnyException();
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
