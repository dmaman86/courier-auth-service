package com.courier.authservice.service;

import com.courier.authservice.config.security.CustomUserDetails;
import com.courier.authservice.objects.dto.AuthResponse;
import com.courier.authservice.objects.dto.LoginDto;

import jakarta.servlet.http.Cookie;

public interface AuthService {

  AuthResponse login(LoginDto loginDto);

  AuthResponse logout(Long userId);

  AuthResponse refreshToken(CustomUserDetails userDetails, Cookie refreshTokenCookie);
}
