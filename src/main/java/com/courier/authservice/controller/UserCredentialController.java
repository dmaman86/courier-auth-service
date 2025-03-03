package com.courier.authservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courier.authservice.config.security.CustomUserDetails;
import com.courier.authservice.objects.dto.AuthResponse;
import com.courier.authservice.objects.dto.UpdatePasswordDto;
import com.courier.authservice.service.AuthService;
import com.courier.authservice.service.UserCredentialService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/credential")
public class UserCredentialController {

  @Autowired private UserCredentialService userCredentialService;

  @Autowired private AuthService authService;

  @PostMapping("/reset-password")
  public ResponseEntity<String> resetUserPassword(
      @RequestBody UpdatePasswordDto updatePasswordDto, Authentication authentication) {

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    if (!userDetails.getId().equals(updatePasswordDto.getUserId())) {
      throw new AccessDeniedException("Your are not allowed to change another user's password");
    }

    userCredentialService.updatePassword(updatePasswordDto);
    return ResponseEntity.ok("Password updated successfully");
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse response) {
    Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
    AuthResponse authResponse = authService.logout(userId);
    setAuthCookies(response, authResponse);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<Void> refreshToken(
      @CookieValue("refreshToken") Cookie refreshTokenCookie,
      Authentication authentication,
      HttpServletResponse response) {

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    AuthResponse authResponse = authService.refreshToken(userDetails, refreshTokenCookie);
    setAuthCookies(response, authResponse);
    return ResponseEntity.noContent().build();
  }

  private void setAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
    response.addCookie(authResponse.getAccessTokenCookie());
    response.addCookie(authResponse.getRefreshTokenCookie());
  }
}
