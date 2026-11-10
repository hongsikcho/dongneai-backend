package com.dongneai.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-0123456789abcdef-0123456789";
    private static final Instant NOW = Instant.parse("2026-10-27T00:00:00Z");

    private final JwtProperties properties =
            new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofDays(30));

    @Test
    void 발급한_토큰을_검증하면_사용자와_권한이_나온다() {
        JwtTokenProvider provider = new JwtTokenProvider(properties, Clock.fixed(NOW, ZoneOffset.UTC));

        AuthUser user = provider.parseAccessToken(provider.createAccessToken(42L, "USER"));

        assertThat(user).isEqualTo(new AuthUser(42L, "USER"));
    }

    @Test
    void 만료된_토큰은_ExpiredJwtException() {
        String token = new JwtTokenProvider(properties, Clock.fixed(NOW, ZoneOffset.UTC))
                .createAccessToken(1L, "USER");
        JwtTokenProvider later = new JwtTokenProvider(
                properties, Clock.fixed(NOW.plus(Duration.ofMinutes(31)), ZoneOffset.UTC));

        assertThatThrownBy(() -> later.parseAccessToken(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void 다른_키로_서명된_토큰은_거부한다() {
        String token = new JwtTokenProvider(properties, Clock.fixed(NOW, ZoneOffset.UTC))
                .createAccessToken(1L, "USER");
        JwtProperties other = new JwtProperties("another-secret-0123456789abcdef-012345", Duration.ofMinutes(30),
                Duration.ofDays(30));

        assertThatThrownBy(() -> new JwtTokenProvider(other, Clock.fixed(NOW, ZoneOffset.UTC)).parseAccessToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void 짧은_secret은_기동시_실패한다() {
        assertThatThrownBy(() -> new JwtTokenProvider(
                new JwtProperties("short", Duration.ofMinutes(30), Duration.ofDays(30))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refresh_토큰_해시는_64자_hex() {
        assertThat(JwtTokenProvider.hash("abc")).hasSize(64).matches("[0-9a-f]+");
    }
}
