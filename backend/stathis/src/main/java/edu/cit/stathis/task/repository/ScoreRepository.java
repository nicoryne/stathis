package edu.cit.stathis.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.task.entity.Score;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, UUID> {
    Score findByPhysicalId(String physicalId);
    List<Score> findByStudentId(String studentId);
    List<Score> findByTaskId(String taskId);
    Score findByStudentIdAndTaskIdAndQuizTemplateId(String studentId, String taskId, String quizTemplateId);
    boolean existsByPhysicalId(String physicalId);
    List<Score> findByStudentIdAndTaskId(String studentId, String taskId);
} 