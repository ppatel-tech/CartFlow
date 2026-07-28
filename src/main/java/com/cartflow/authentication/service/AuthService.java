package com.cartflow.authentication.service;

import com.cartflow.authentication.dto.request.*;
import com.cartflow.authentication.dto.response.LoginResponse;
import com.cartflow.authentication.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void logout(LogoutRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}