package com.courier.authservice.objects.dto;

import java.time.LocalDateTime;

import com.courier.authservice.objects.enums.ErrorSeverity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorLogDto {

  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private String exception;

  @Enumerated(EnumType.STRING)
  private ErrorSeverity severity;
}
