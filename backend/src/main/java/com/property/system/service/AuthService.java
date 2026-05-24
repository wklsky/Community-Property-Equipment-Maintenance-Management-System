package com.property.system.service;

import com.property.system.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(String refreshToken);

    void sendCode(SmsCodeSendRequest request);

    LoginResponse loginByCode(SmsCodeLoginRequest request);

    void resetPassword(ResetPasswordRequest request);

    void register(RegisterRequest request);
}
