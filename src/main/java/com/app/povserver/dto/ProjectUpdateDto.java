package com.app.povserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateDto {
  private String title;
  private Integer defaultRpm;
  private Integer defaultBrightness;
}