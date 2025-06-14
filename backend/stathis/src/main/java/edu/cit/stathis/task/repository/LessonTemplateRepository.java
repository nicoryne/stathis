package edu.cit.stathis.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.task.entity.LessonTemplate;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LessonTemplateRepository extends JpaRepository<LessonTemplate, UUID> {
    LessonTemplate findByTitle(String title);
    Optional<LessonTemplate> findByPhysicalId(String physicalId);
}
