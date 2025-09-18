package edu.cit.stathis.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.stathis.task.entity.ExerciseTemplate;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface ExerciseTemplateRepository extends JpaRepository<ExerciseTemplate, UUID> {
    ExerciseTemplate findByTitle(String title);
    Optional<ExerciseTemplate> findByPhysicalId(String physicalId);
    List<ExerciseTemplate> findByTeacherPhysicalId(String teacherPhysicalId);
    void deleteByPhysicalId(String physicalId);
}
