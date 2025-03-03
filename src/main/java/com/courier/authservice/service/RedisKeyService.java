package com.courier.authservice.service;

public interface RedisKeyService {

  void setPublicKey(String publicKey);

  void setPrivateKey(String privateKey);

  void setAuthServiceSecret(String authServiceSecret);

  String getPublicKey();

  String getPrivateKey();

  String getAuthServiceSecret();

  boolean tryAcquire();

  void acquire();

  void release();
}
