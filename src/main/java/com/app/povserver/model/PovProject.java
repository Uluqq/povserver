package com.app.povserver.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pov_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PovProject {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String title;

  @Builder.Default
  private Integer ledCount = 32;

  @Builder.Default
  private Integer sectorsCount = 256;

  // Исходное изображение (PNG/JPG) для показа в телефоне
  @Lob
  @Column(name = "original_image")
  private byte[] originalImage;

  // Готовая бинарная полярная матрица [256 * 32 * 3 байт] для ESP32
  @Lob
  @Column(name = "polar_matrix")
  private byte[] polarMatrix;

  @Builder.Default
  private Integer defaultRpm = 1500;

  @Builder.Default
  private Integer defaultBrightness = 80;

  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}