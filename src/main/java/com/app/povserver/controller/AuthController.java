package com.app.povserver.controller;

import com.app.povserver.dto.AuthRequest;
import com.app.povserver.dto.AuthResponse;
import com.app.povserver.model.User;
import com.app.povserver.repository.UserRepository;
import com.app.povserver.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody AuthRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body("Пользователь с таким именем уже существует");
    }

    User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

    userRepository.save(user);

    String token = jwtService.generateToken(user.getUsername());
    return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody AuthRequest request) {
    User user = userRepository.findByUsername(request.getUsername()).orElse(null);

    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body("Неверное имя пользователя или пароль");
    }

    String token = jwtService.generateToken(user.getUsername());
    return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
  }
}