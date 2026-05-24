package com.property.system.controller;

import com.property.system.dto.*;
import com.property.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SmsCodeSendRequest request) {
        authService.sendCode(request);
        return Result.success();
    }

    @PostMapping("/login-by-code")
    public Result<LoginResponse> loginByCode(@Valid @RequestBody SmsCodeLoginRequest request) {
        return Result.success(authService.loginByCode(request));
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refresh(request.getRefreshToken()));
    }
}
