package com.app.povserver.service;

import com.app.povserver.dto.AuthRequest;
import com.app.povserver.dto.AuthResponse;
import com.app.povserver.model.User;
import com.app.povserver.repository.UserRepository;
import com.app.povserver.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResponse register(AuthRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Пользователь с таким логином уже существует");
    }

    User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

    userRepository.save(user);
    String token = jwtService.generateToken(user.getUsername());
    return new AuthResponse(token, user.getUsername());
  }

  public AuthResponse login(AuthRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Неверный логин или пароль");
    }

    String token = jwtService.generateToken(user.getUsername());
    return new AuthResponse(token, user.getUsername());
  }
}