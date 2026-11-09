package com.app.povserver.controller;

import com.app.povserver.dto.ProjectResponseDto;
import com.app.povserver.dto.ProjectUpdateDto;
import com.app.povserver.model.PovProject;
import com.app.povserver.model.User;
import com.app.povserver.repository.PovProjectRepository;
import com.app.povserver.repository.UserRepository;
import com.app.povserver.service.PovMatrixConverterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class PovProjectController {

  private final PovProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final PovMatrixConverterService converterService;

  // 1. Получить все проекты пользователя
  @GetMapping
  public ResponseEntity<List<ProjectResponseDto>> getUserProjects(Principal principal) {
    User user = userRepository.findByUsername(principal.getName()).orElseThrow();

    List<ProjectResponseDto> response = projectRepository.findAllByUserOrderByCreatedAtDesc(user)
            .stream()
            .map(this::mapToDto)
            .toList();

    return ResponseEntity.ok(response);
  }

  // 2. Получить один проект по ID
  @GetMapping("/{id}")
  public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id, Principal principal) {
    User user = userRepository.findByUsername(principal.getName()).orElseThrow();
    PovProject project = projectRepository.findByIdAndUser(id, user).orElseThrow();
    return ResponseEntity.ok(mapToDto(project));
  }

  // 3. Загрузка фото и генерация матрицы
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ProjectResponseDto> uploadProject(
          @RequestParam("file") MultipartFile file,
          @RequestParam("title") String title,
          @RequestParam(value = "ledCount", defaultValue = "32") int ledCount,
          @RequestParam(value = "sectorsCount", defaultValue = "256") int sectorsCount,
          Principal principal
  ) throws Exception {
    User user = userRepository.findByUsername(principal.getName()).orElseThrow();

    byte[] rawImage = file.getBytes();
    byte[] polarMatrix = converterService.convertToPolarMatrix(
            rawImage,
            ledCount,
            sectorsCount,
            PovMatrixConverterService.ColorOrder.GRB
    );

    PovProject project = PovProject.builder()
            .user(user)
            .title(title)
            .ledCount(ledCount)
            .sectorsCount(sectorsCount)
            .originalImage(rawImage)
            .polarMatrix(polarMatrix)
            .build();

    PovProject saved = projectRepository.save(project);
    return ResponseEntity.ok(mapToDto(saved));
  }

  // 4. Обновление параметров проекта (RPM, яркость, название)
  @PatchMapping("/{id}")
  public ResponseEntity<ProjectResponseDto> updateProject(
          @PathVariable Long id,
          @RequestBody ProjectUpdateDto dto,
          Principal principal
  ) {
    User user = userRepository.findByUsername(principal.getName()).orElseThrow();
    PovProject project = projectRepository.findByIdAndUser(id, user).orElseThrow();

    if (dto.getTitle() != null) project.setTitle(dto.getTitle());
    if (dto.getDefaultRpm() != null) project.setDefaultRpm(dto.getDefaultRpm());
    if (dto.getDefaultBrightness() != null) project.setDefaultBrightness(dto.getDefaultBrightness());

    PovProject updated = projectRepository.save(project);
    return ResponseEntity.ok(mapToDto(updated));
  }

  // 5. Удаление проекта
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProject(@PathVariable Long id, Principal principal) {
    User user = userRepository.findByUsername(principal.getName()).orElseThrow();
    PovProject project = projectRepository.findByIdAndUser(id, user).orElseThrow();
    projectRepository.delete(project);
    return ResponseEntity.noContent().build();
  }

  // 6. Получить исходную картинку (PNG/JPG)
  @GetMapping("/{id}/image")
  public ResponseEntity<byte[]> getProjectImage(@PathVariable Long id) {
    PovProject project = projectRepository.findById(id).orElseThrow();
    return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(project.getOriginalImage());
  }

  // 7. Получить бинарный буфер матрицы для ESP32
  @GetMapping("/{id}/matrix")
  public ResponseEntity<byte[]> getProjectMatrix(@PathVariable Long id) {
    PovProject project = projectRepository.findById(id).orElseThrow();
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"matrix_" + id + ".bin\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(project.getPolarMatrix());
  }

  // Маппер сущности в DTO
  private ProjectResponseDto mapToDto(PovProject p) {
    return ProjectResponseDto.builder()
            .id(p.getId())
            .title(p.getTitle())
            .ledCount(p.getLedCount())
            .sectorsCount(p.getSectorsCount())
            .defaultRpm(p.getDefaultRpm())
            .defaultBrightness(p.getDefaultBrightness())
            .matrixSizeBytes(p.getPolarMatrix() != null ? p.getPolarMatrix().length : 0)
            .imageUrl("/api/projects/" + p.getId() + "/image")
            .matrixUrl("/api/projects/" + p.getId() + "/matrix")
            .createdAt(p.getCreatedAt())
            .build();
  }
}