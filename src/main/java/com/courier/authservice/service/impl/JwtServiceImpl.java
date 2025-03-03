package com.courier.authservice.service.impl;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courier.authservice.config.security.CustomUserDetails;
import com.courier.authservice.exception.PublicKeyException;
import com.courier.authservice.objects.entity.RefreshToken;
import com.courier.authservice.repository.RefreshTokenRepository;
import com.courier.authservice.service.JwtService;
import com.courier.authservice.service.RedisKeyService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtServiceImpl implements JwtService {

  private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

  private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 min
  private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

  @Autowired private RedisKeyService redisKeyService;

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Override
  public boolean isTokenValid(String token) {
    return extractExpiration(token).after(new Date());
  }

  @Override
  public boolean isRefreshTokenValid(String token) {
    Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);

    if (refreshToken.isPresent()) {
      return isTokenValid(token);
    }
    return false;
  }

  @Override
  public String generateToken(CustomUserDetails userDetails, long expirationTime) {
    try {
      PrivateKey privateKey = convertToPrivateKey(redisKeyService.getPrivateKey());

      Map<String, Object> claims =
          Map.of(
              "id", userDetails.getId(),
              "fullName", userDetails.getFullName(),
              "email", userDetails.getUsername(),
              "phoneNumber", userDetails.getPhoneNumber(),
              "roles",
                  userDetails.getAuthorities().stream()
                      .map(GrantedAuthority::getAuthority)
                      .collect(Collectors.toList()));

      return Jwts.builder()
          .setSubject(userDetails.getUsername())
          .setClaims(claims)
          .setIssuedAt(new Date(System.currentTimeMillis()))
          .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
          .signWith(privateKey, SignatureAlgorithm.RS256)
          .compact();
    } catch (Exception e) {
      logger.error("Error generating token: ", e);
      throw new RuntimeException("Error generating JWT token", e);
    }
  }

  @Override
  public long getAccessTokenExpirationTime() {
    return ACCESS_TOKEN_EXPIRATION;
  }

  @Override
  public long getRefreshTokenExpirationTime() {
    return REFRESH_TOKEN_EXPIRATION;
  }

  @Override
  public CustomUserDetails getUserDetails(String token) {
    Claims claims = parseTokenClaims(token);

    Collection<? extends GrantedAuthority> authorities =
        ((List<?>) claims.get("roles"))
            .stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

    return new CustomUserDetails(
        Long.parseLong(claims.get("id").toString()),
        claims.get("fullName", String.class),
        claims.get("email", String.class),
        claims.get("phoneNumber", String.class),
        authorities);
  }

  @Override
  @Transactional
  public void deleteUserToken(Long userId) {
    refreshTokenRepository.deleteByUserId(userId);
  }

  @Override
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = parseTokenClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims parseTokenClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(convertToPublicKey(redisKeyService.getPublicKey()))
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private <T extends KeySpec, K extends Key> K convertToKey(
      String key, Function<T, K> keyGenerator, Class<T> keySpecClass) {
    try {
      byte[] keyBytes = Base64.getDecoder().decode(key);
      T keySpec = keySpecClass.getConstructor(byte[].class).newInstance((Object) keyBytes);
      return keyGenerator.apply(keySpec);
    } catch (Exception e) {
      logger.error("Error converting key: ", e);
      throw new PublicKeyException("Error converting key");
    }
  }

  private PrivateKey convertToPrivateKey(String privateKey) {
    return convertToKey(
        privateKey,
        spec -> {
          try {
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        PKCS8EncodedKeySpec.class);
  }

  private PublicKey convertToPublicKey(String publicKey) {
    return convertToKey(
        publicKey,
        spec -> {
          try {
            return KeyFactory.getInstance("RSA").generatePublic(spec);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        X509EncodedKeySpec.class);
  }
}
