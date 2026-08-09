package com.cartflow.authentication.service;

import com.cartflow.authentication.dto.request.*;
import com.cartflow.authentication.dto.response.LoginResponse;
import com.cartflow.authentication.dto.response.RegisterResponse;
import com.cartflow.authentication.entity.PasswordResetToken;
import com.cartflow.authentication.entity.RefreshToken;
import com.cartflow.authentication.repository.PasswordResetTokenRepository;
import com.cartflow.authentication.repository.RefreshTokenRepository;
import com.cartflow.cart.entity.Cart;
import com.cartflow.cart.repository.CartRepository;
import com.cartflow.exception.DuplicateResourceException;
import com.cartflow.exception.InvalidTokenException;
import com.cartflow.security.JwtUtil;
import com.cartflow.security.UserPrincipal;
import com.cartflow.user.entity.User;
import com.cartflow.user.entity.UserRole;
import com.cartflow.user.entity.UserStatus;
import com.cartflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtil jwtUtil;
    private final CartRepository cartRepository;


    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.password-reset.token-expiration-ms}")
    private long passwordResetTokenExpirationMs;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with email '" + request.getEmail() + "' already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.ROLE_CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        Cart cart = Cart.builder()
                .user(savedUser)
                .build();

        cartRepository.save(cart);

        log.info("Cart initialized for user: {}", savedUser.getEmail());

        log.info("New user registered with email: {}", savedUser.getEmail());

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }



    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtUtil.generateAccessToken(
                principal.getEmail(), principal.getRole());

        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(userRepository.findByEmail(principal.getEmail()).orElseThrow())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in: {}", principal.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .userId(principal.getId())
                .email(principal.getEmail())
                .role(principal.getRole())
                .build();
    }


    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken existingToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (existingToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existingToken);
            throw new InvalidTokenException("Refresh token has expired, please login again");
        }

        User user = existingToken.getUser();

        refreshTokenRepository.delete(existingToken);

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());

        String newRefreshTokenValue = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        log.info("Access token refreshed for user: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }


    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshTokenRepository.delete(token);

        log.info("User logged out: {}", token.getUser().getEmail());
    }


    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {

            String resetTokenValue = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(resetTokenValue)
                    .user(user)
                    .expiryDate(Instant.now().plusMillis(passwordResetTokenExpirationMs))
                    .build();

            passwordResetTokenRepository.save(resetToken);

            // TODO (Phase 13): replace with real email service.
            log.info("[SIMULATED EMAIL] Password reset link for {}: " +
                            "http://localhost:8080/api/v1/auth/reset-password?token={}",
                    user.getEmail(), resetTokenValue);
        });

        // Always the same response regardless of whether the email existed —
        // prevents user enumeration (see Checkpoint 2.12 discussion).
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Reset token has expired, please request a new one");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        refreshTokenRepository.deleteByUser(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }


}