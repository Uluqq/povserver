package com.app.povserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {
  private Long id;
  private String title;
  private Integer ledCount;
  private Integer sectorsCount;
  private Integer defaultRpm;
  private Integer defaultBrightness;
  private int matrixSizeBytes;
  private String imageUrl;
  private String matrixUrl;
  private LocalDateTime createdAt;
}