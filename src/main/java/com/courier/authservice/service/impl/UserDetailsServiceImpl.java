package com.courier.authservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.courier.authservice.config.security.CustomUserDetails;
import com.courier.authservice.feignclient.UserServiceClient;
import com.courier.authservice.objects.dto.LoginDto;
import com.courier.authservice.objects.dto.UserDto;
import com.courier.authservice.objects.entity.UserCredential;
import com.courier.authservice.repository.UserCredentialRepository;
import com.courier.authservice.service.RedisKeyService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired private UserServiceClient userServiceClient;

  @Autowired private UserCredentialRepository userCredentialRepository;

  @Autowired private RedisKeyService redisKeyService;

  public UserDetails loadUser(LoginDto loginDto) throws UsernameNotFoundException {
    String apiKey = redisKeyService.getAuthServiceSecret();

    if (apiKey == null) {
      throw new RuntimeException("API key not found");
    }

    UserDto userDto =
        userServiceClient.getUserByEmailOrPhone(loginDto.getEmail(), loginDto.getPhone(), apiKey);
    if (userDto == null || userDto.getId() == null) {
      throw new UsernameNotFoundException(
          "User not found with email or phone: " + loginDto.getEmail() + " " + loginDto.getPhone());
    }

    UserCredential userCredential =
        userCredentialRepository
            .findByUserId(userDto.getId())
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        "User credentials not found for user id: " + userDto.getId()));

    return new CustomUserDetails(userDto, userCredential);
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return null;
  }
}
