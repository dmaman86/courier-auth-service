package com.courier.authservice.service.impl;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.courier.authservice.objects.dto.AuthInfoDto;
import com.courier.authservice.service.EventProducerService;
import com.courier.authservice.service.RedisKeyService;

@Service
public class RedisKeyServiceImpl implements RedisKeyService {

  private static final Logger logger = LoggerFactory.getLogger(RedisKeyServiceImpl.class);

  @Autowired private StringRedisTemplate redisTemplate;

  @Autowired private EventProducerService eventProducerService;

  private final Semaphore semaphore = new Semaphore(1);

  private static final String PUBLIC_KEY_KEY = "publicKey";
  private static final String PRIVATE_KEY_KEY = "privateKey";
  private static final String AUTH_SECRET_KEY = "authServiceSecret";

  @Override
  public void setPublicKey(String publicKey) {
    redisTemplate.opsForValue().set(PUBLIC_KEY_KEY, publicKey);
  }

  @Override
  public void setPrivateKey(String privateKey) {
    redisTemplate.opsForValue().set(PRIVATE_KEY_KEY, privateKey);
  }

  @Override
  public void setAuthServiceSecret(String authServiceSecret) {
    redisTemplate.opsForValue().set(AUTH_SECRET_KEY, authServiceSecret);
  }

  @Override
  public String getPublicKey() {
    return redisTemplate.opsForValue().get(PUBLIC_KEY_KEY);
  }

  @Override
  public String getPrivateKey() {
    return redisTemplate.opsForValue().get(PRIVATE_KEY_KEY);
  }

  @Override
  public String getAuthServiceSecret() {
    return redisTemplate.opsForValue().get(AUTH_SECRET_KEY);
  }

  @Override
  public boolean tryAcquire() {
    return semaphore.tryAcquire();
  }

  @Override
  public void acquire() {
    try {
      semaphore.acquire();
      logger.info("Semaphore acquired");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Error acquiring semaphore", e);
    }
  }

  @Override
  public void release() {
    semaphore.release();
    logger.info("Semaphore released");

    if (getPublicKey() != null && getAuthServiceSecret() != null) {
      sendAuthInfoKeys();
    }
  }

  private void sendAuthInfoKeys() {
    logger.info("Sending auth info keys");

    AuthInfoDto authInfoDto =
        AuthInfoDto.builder()
            .publicKey(getPublicKey())
            .authServiceSecret(getAuthServiceSecret())
            .build();

    eventProducerService.sendAuthInfoEvent(authInfoDto);
    logger.info("Sent public key to Kafka: {}", authInfoDto);
  }
}
