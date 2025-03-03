package com.courier.authservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courier.authservice.objects.dto.AuthResponse;
import com.courier.authservice.objects.dto.LoginDto;
import com.courier.authservice.objects.dto.SetPasswordDto;
import com.courier.authservice.service.AuthService;
import com.courier.authservice.service.UserCredentialService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired private AuthService authService;

  @Autowired private UserCredentialService userCredentialService;

  @PostMapping("/login")
  public ResponseEntity<Void> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
    AuthResponse authResponse = authService.login(loginDto);
    setAuthCookies(response, authResponse);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/set-password")
  public ResponseEntity<Void> setUserPassword(@RequestBody SetPasswordDto setPasswordDto) {
    userCredentialService.setPassword(setPasswordDto);
    return ResponseEntity.noContent().build();
  }

  private void setAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
    response.addCookie(authResponse.getAccessTokenCookie());
    response.addCookie(authResponse.getRefreshTokenCookie());
  }
}
