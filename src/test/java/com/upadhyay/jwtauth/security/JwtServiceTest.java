package com.upadhyay.jwtauth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.upadhyay.jwtauth.config.AppProperties;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.getJwt().setSecret("test-secret-key-for-jwt-hs256-at-least-256-bits-long-unit-test-only");
        props.getJwt().setAccessTokenExpirationMs(900_000L);
        props.getJwt().setRefreshTokenExpirationMs(604_800_000L);
        props.getJwt().setIssuer("jwt-auth-api-test");

        jwtService = new JwtService(props);
        user = new User("himanshu", "ignored",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Access token contains subject and is marked as 'access' type")
    void accessTokenCarriesSubjectAndType() {
        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("himanshu");
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("Refresh token is distinguishable from access token")
    void refreshTokenTypeIsDetected() {
        String refresh = jwtService.generateRefreshToken(user);

        assertThat(refresh).isNotBlank();
        assertThat(jwtService.isRefreshToken(refresh)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns true when subject matches and token not expired")
    void validTokenPassesValidation() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false when username differs")
    void mismatchedUserFailsValidation() {
        String token = jwtService.generateAccessToken(user);
        UserDetails other = new User("someone-else", "ignored", Collections.emptyList());

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    @DisplayName("Tampered token throws JwtException on parse")
    void tamperedTokenRejected() {
        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatCode(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(JwtService.JwtException.class);
    }
}
