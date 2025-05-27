package edu.cit.stathis.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.task.entity.QuizTemplate;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface QuizTemplateRepository extends JpaRepository<QuizTemplate, UUID> {
    QuizTemplate findByTitle(String title);
    Optional<QuizTemplate> findByPhysicalId(String physicalId);
}
