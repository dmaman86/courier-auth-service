package com.courier.authservice.service;

public interface BlackListService {

  void handleUserDisabledEvent(Long userId);

  void cleanExpiredBlackListUsers();

  boolean isUserBlackListed(Long userId);
}
