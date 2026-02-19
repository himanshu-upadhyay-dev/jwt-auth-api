package com.upadhyay.jwtauth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.upadhyay.jwtauth.dto.request.LoginRequest;
import com.upadhyay.jwtauth.dto.request.SignupRequest;
import com.upadhyay.jwtauth.dto.response.AuthResponse;
import com.upadhyay.jwtauth.entity.Role;
import com.upadhyay.jwtauth.entity.User;
import com.upadhyay.jwtauth.exception.InvalidCredentialsException;
import com.upadhyay.jwtauth.exception.UserAlreadyExistsException;
import com.upadhyay.jwtauth.repository.UserRepository;
import com.upadhyay.jwtauth.security.CustomUserDetails;
import com.upadhyay.jwtauth.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private SignupRequest signupRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        signupRequest = SignupRequest.builder()
                .username("himanshu")
                .email("himanshu@example.com")
                .password("Str0ngPass1")
                .fullName("Himanshu Upadhyay")
                .build();

        savedUser = User.builder()
                .id(1L)
                .username("himanshu")
                .email("himanshu@example.com")
                .password("encoded")
                .fullName("Himanshu Upadhyay")
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("register persists a new user and returns token pair")
    void registerCreatesUserAndReturnsTokens() {
        when(userRepository.existsByUsername("himanshu")).thenReturn(false);
        when(userRepository.existsByEmail("himanshu@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Str0ngPass1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername("himanshu")).thenReturn(new CustomUserDetails(savedUser));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900_000L);

        AuthResponse response = authService.register(signupRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getUsername()).isEqualTo("himanshu");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register rejects a duplicate username")
    void registerFailsWhenUsernameExists() {
        when(userRepository.existsByUsername("himanshu")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(signupRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register rejects a duplicate email")
    void registerFailsWhenEmailExists() {
        when(userRepository.existsByUsername("himanshu")).thenReturn(false);
        when(userRepository.existsByEmail("himanshu@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(signupRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login returns tokens when credentials are valid")
    void loginSucceedsWithValidCredentials() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("himanshu")
                .password("Str0ngPass1")
                .build();

        CustomUserDetails principal = new CustomUserDetails(savedUser);
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(principal);
        when(userRepository.findByUsername("himanshu")).thenReturn(java.util.Optional.of(savedUser));
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900_000L);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getEmail()).isEqualTo("himanshu@example.com");
    }

    @Test
    @DisplayName("login throws InvalidCredentialsException on bad password")
    void loginFailsWithBadCredentials() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("himanshu")
                .password("wrong")
                .build();

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
