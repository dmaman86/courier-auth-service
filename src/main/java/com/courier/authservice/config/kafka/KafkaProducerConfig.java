package com.courier.authservice.config.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.courier.authservice.objects.dto.AuthInfoDto;
import com.courier.authservice.objects.dto.ErrorLogDto;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public <T> ProducerFactory<String, T> producerFactory(Class<T> valueType) {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, ErrorLogDto> errorLogDtoKafkaTemplate() {
    return new KafkaTemplate<>(producerFactory(ErrorLogDto.class));
  }

  public KafkaTemplate<String, AuthInfoDto> authInfoDtoKafkaTemplate() {
    return new KafkaTemplate<>(producerFactory(AuthInfoDto.class));
  }
}
