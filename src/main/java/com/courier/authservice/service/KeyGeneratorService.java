package com.courier.authservice.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class KeyGeneratorService {

  private static final Logger logger = LoggerFactory.getLogger(KeyGeneratorService.class);

  @Autowired private RedisKeyService redisKeyService;

  @PostConstruct
  public void init() {
    generateKeys();
  }

  @Scheduled(cron = "0 0 0 1 */2 ?") // Every 2 months
  public void rotateKeys() {
    logger.info("Executing key rotation");
    generateKeys();
  }

  private void generateKeys() {
    if (redisKeyService.tryAcquire()) {
      try {

        logger.info("Generating keys");
        KeyPair keyPair = generateKeyPair();
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String authServiceSecret = UUID.randomUUID().toString();

        redisKeyService.setPublicKey(publicKey);
        redisKeyService.setPrivateKey(privateKey);
        redisKeyService.setAuthServiceSecret(authServiceSecret);

        logger.info("Keys generated and saved to redis");

      } catch (Exception e) {
        logger.error("Error generating keys: ", e);
        redisKeyService.release();
        CompletableFuture.runAsync(() -> generateKeys());
      } finally {
        redisKeyService.release();
      }
    }
  }

  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.generateKeyPair();
  }
}
