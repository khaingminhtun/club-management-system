package com.jvproject.club_management_system.service;

import com.jvproject.club_management_system.dto.auth.*;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    VerifyEmailResponse verifyEmail(VerifyEmailRequest request);

    ResendOtpResponse resendOtp(ResendOtpRequest request);

    LoginResult login(LoginRequest request,String deviceName,String ipAddress);

    RefreshTokenResult refresh(String refreshToken);

    void logout(String refreshToken);
}
