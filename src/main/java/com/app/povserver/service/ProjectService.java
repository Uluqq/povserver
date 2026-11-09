package com.app.povserver.service;

import com.app.povserver.dto.ProjectResponseDto;
import com.app.povserver.dto.ProjectUpdateDto;
import com.app.povserver.exception.ResourceNotFoundException;
import com.app.povserver.model.PovProject;
import com.app.povserver.model.User;
import com.app.povserver.repository.PovProjectRepository;
import com.app.povserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private final PovProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final PovMatrixConverterService converterService;

  public List<ProjectResponseDto> getUserProjects(String username) {
    User user = getUserByUsername(username);
    return projectRepository.findAllByUserOrderByCreatedAtDesc(user)
            .stream()
            .map(this::mapToDto)
            .toList();
  }

  public ProjectResponseDto getProjectById(Long id, String username) {
    User user = getUserByUsername(username);
    PovProject project = projectRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Проект не найден"));
    return mapToDto(project);
  }

  public ProjectResponseDto createProject(
          MultipartFile file,
          String title,
          int ledCount,
          int sectorsCount,
          String username
  ) throws IOException {
    User user = getUserByUsername(username);

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

    return mapToDto(projectRepository.save(project));
  }

  public ProjectResponseDto updateProject(Long id, ProjectUpdateDto dto, String username) {
    User user = getUserByUsername(username);
    PovProject project = projectRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Проект не найден"));

    if (dto.getTitle() != null) project.setTitle(dto.getTitle());
    if (dto.getDefaultRpm() != null) project.setDefaultRpm(dto.getDefaultRpm());
    if (dto.getDefaultBrightness() != null) project.setDefaultBrightness(dto.getDefaultBrightness());

    return mapToDto(projectRepository.save(project));
  }

  public void deleteProject(Long id, String username) {
    User user = getUserByUsername(username);
    PovProject project = projectRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Проект не найден"));
    projectRepository.delete(project);
  }

  public PovProject getProjectEntity(Long id) {
    return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Проект не найден"));
  }

  private User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
  }

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