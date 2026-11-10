package com.dongneai.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * secret은 HS256 기준 32바이트(256bit) 이상. 반드시 환경변수 JWT_SECRET으로 주입한다.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {
}
