package com.courier.authservice.service.impl;

import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.courier.authservice.config.security.CustomUserDetails;
import com.courier.authservice.objects.dto.AuthResponse;
import com.courier.authservice.objects.dto.LoginDto;
import com.courier.authservice.objects.entity.RefreshToken;
import com.courier.authservice.repository.RefreshTokenRepository;
import com.courier.authservice.service.AuthService;
import com.courier.authservice.service.JwtService;

import jakarta.servlet.http.Cookie;

@Service
public class AuthServiceImpl implements AuthService {

  @Autowired private JwtService jwtService;

  @Autowired private UserDetailsServiceImpl userDetailsService;

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Autowired private AuthenticationManager authenticationManager;

  @Override
  public AuthResponse login(LoginDto loginDto) {
    UserDetails userDetails = userDetailsService.loadUser(loginDto);

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(), loginDto.getPassword()));

    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

    String accessToken =
        jwtService.generateToken(customUserDetails, jwtService.getAccessTokenExpirationTime());
    String refreshToken =
        jwtService.generateToken(customUserDetails, jwtService.getRefreshTokenExpirationTime());

    saveRefreshToken(refreshToken, customUserDetails.getId());

    return AuthResponse.builder()
        .accessTokenCookie(
            createCookie("accessToken", accessToken, jwtService.getAccessTokenExpirationTime()))
        .refreshTokenCookie(
            createCookie("refreshToken", refreshToken, jwtService.getRefreshTokenExpirationTime()))
        .build();
  }

  @Override
  public AuthResponse logout(Long userId) {
    jwtService.deleteUserToken(userId);
    return AuthResponse.builder()
        .accessTokenCookie(createCookie("accessToken", "", 0))
        .refreshTokenCookie(createCookie("refreshToken", "", 0))
        .build();
  }

  @Override
  public AuthResponse refreshToken(CustomUserDetails userDetails, Cookie refreshTokenCookie) {
    String newAccessToken =
        jwtService.generateToken(userDetails, jwtService.getAccessTokenExpirationTime());

    return AuthResponse.builder()
        .accessTokenCookie(
            createCookie("accessToken", newAccessToken, jwtService.getAccessTokenExpirationTime()))
        .refreshTokenCookie(refreshTokenCookie)
        .build();
  }

  private void saveRefreshToken(String token, Long userId) {

    Date expirationDate = jwtService.extractExpiration(token);

    RefreshToken refreshToken =
        RefreshToken.builder()
            .token(token)
            .userId(userId)
            .expirationDate(
                expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
            .build();
    refreshTokenRepository.save(refreshToken);
  }

  private Cookie createCookie(String tokenType, String token, long expirationTime) {
    Cookie cookie = new Cookie(tokenType, token);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // change to true in production
    cookie.setMaxAge((int) (expirationTime / 1000)); // convert milliseconds to seconds
    cookie.setPath("/");
    return cookie;
  }
}
