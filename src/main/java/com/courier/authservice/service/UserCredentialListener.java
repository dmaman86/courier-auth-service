package com.courier.authservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.courier.authservice.objects.dto.UserDto;

@Component
public class UserCredentialListener {

  @Autowired private UserCredentialService userCredentialService;

  private static final Logger logger = LoggerFactory.getLogger(UserCredentialListener.class);

  @KafkaListener(
      topics = "user-created",
      groupId = "auth-service-group",
      containerFactory = "userDtoKafkaListenerContainerFactory")
  public void handleUserCreated(UserDto userDto) {
    logger.info("Get user to create: {}", userDto.getId());
    userCredentialService.createUserCredential(userDto);
  }
}
