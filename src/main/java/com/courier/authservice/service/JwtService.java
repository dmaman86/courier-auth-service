package com.courier.authservice.service;

import java.util.Date;

import com.courier.authservice.config.security.CustomUserDetails;

public interface JwtService {

  boolean isTokenValid(String token);

  boolean isRefreshTokenValid(String token);

  String generateToken(CustomUserDetails userDetails, long expirationTime);

  long getAccessTokenExpirationTime();

  long getRefreshTokenExpirationTime();

  CustomUserDetails getUserDetails(String token);

  void deleteUserToken(Long userId);

  Date extractExpiration(String token);
}
