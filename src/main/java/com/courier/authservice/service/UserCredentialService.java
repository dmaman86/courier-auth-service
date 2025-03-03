package com.courier.authservice.service;

import com.courier.authservice.objects.dto.SetPasswordDto;
import com.courier.authservice.objects.dto.UpdatePasswordDto;
import com.courier.authservice.objects.dto.UserDto;

public interface UserCredentialService {

  void createUserCredential(UserDto userDto);

  void setPassword(SetPasswordDto setPasswordDto);

  void updatePassword(UpdatePasswordDto updatePasswordDto);

  void deleteUserCredential(Long userId);
}
