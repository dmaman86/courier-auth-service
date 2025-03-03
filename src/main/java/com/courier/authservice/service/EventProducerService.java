package com.courier.authservice.service;

import com.courier.authservice.objects.dto.AuthInfoDto;
import com.courier.authservice.objects.dto.ErrorLogDto;

public interface EventProducerService {

  void sendErrorEvent(ErrorLogDto errorLogDto);

  void sendAuthInfoEvent(AuthInfoDto authInfoDto);
}
