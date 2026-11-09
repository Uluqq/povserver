package com.app.povserver.repository;

import com.app.povserver.model.PovProject;
import com.app.povserver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PovProjectRepository extends JpaRepository<PovProject, Long> {

  // Получение всех проектов конкретного пользователя с сортировкой (новые сверху)
  List<PovProject> findAllByUserOrderByCreatedAtDesc(User user);

  // Безопасный поиск: получить проект по id только если он принадлежит текущему пользователю
  Optional<PovProject> findByIdAndUser(Long id, User user);

  // Удалить проект пользователя
  void deleteByIdAndUser(Long id, User user);
}