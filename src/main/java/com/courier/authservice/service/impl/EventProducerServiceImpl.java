package com.courier.authservice.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.courier.authservice.objects.dto.AuthInfoDto;
import com.courier.authservice.objects.dto.ErrorLogDto;
import com.courier.authservice.service.EventProducerService;

@Service
public class EventProducerServiceImpl implements EventProducerService {

  private static final Logger logger = LoggerFactory.getLogger(EventProducerServiceImpl.class);

  @Autowired private KafkaTemplate<String, ErrorLogDto> errorLogDtoKafkaTemplate;

  @Autowired private KafkaTemplate<String, AuthInfoDto> authInfoDtoKafkaTemplate;

  @Override
  public void sendErrorEvent(ErrorLogDto errorLogDto) {
    errorLogDtoKafkaTemplate.send("error-topic", errorLogDto);
    logger.info("Sent error log to Kafka: {}", errorLogDto);
  }

  @Override
  public void sendAuthInfoEvent(AuthInfoDto authInfoDto) {
    authInfoDtoKafkaTemplate.send("public-key", authInfoDto);
    logger.info("Sent public key to Kafka: {}", authInfoDto);
  }
}
