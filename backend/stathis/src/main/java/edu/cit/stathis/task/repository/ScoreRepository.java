package edu.cit.stathis.task.repository;

import edu.cit.stathis.task.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoreRepository extends JpaRepository<Score, UUID> {
    
    Optional<Score> findByPhysicalId(String physicalId);
    
    List<Score> findByStudentId(String studentId);
    
    List<Score> findByTaskId(String taskId);
    
    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.taskId = :taskId")
    List<Score> findByStudentIdAndTaskId(@Param("studentId") String studentId, @Param("taskId") String taskId);
    
    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.taskId = :taskId AND s.quizTemplateId = :quizTemplateId")
    Optional<Score> findQuizScore(@Param("studentId") String studentId, @Param("taskId") String taskId, @Param("quizTemplateId") String quizTemplateId);
    
    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.taskId = :taskId AND s.exerciseTemplateId = :exerciseTemplateId")
    Optional<Score> findExerciseScore(@Param("studentId") String studentId, @Param("taskId") String taskId, @Param("exerciseTemplateId") String exerciseTemplateId);
    
    @Query("SELECT AVG(s.score) FROM Score s WHERE s.taskId = :taskId AND s.quizTemplateId = :quizTemplateId")
    Double getAverageQuizScore(@Param("taskId") String taskId, @Param("quizTemplateId") String quizTemplateId);
    
    @Query("SELECT AVG(s.score) FROM Score s WHERE s.taskId = :taskId AND s.exerciseTemplateId = :exerciseTemplateId")
    Double getAverageExerciseScore(@Param("taskId") String taskId, @Param("exerciseTemplateId") String exerciseTemplateId);
    
    boolean existsByPhysicalId(String physicalId);
    
    @Query("SELECT COUNT(s) > 0 FROM Score s WHERE s.studentId = :studentId AND s.taskId = :taskId AND s.quizTemplateId = :quizTemplateId")
    boolean existsQuizScore(@Param("studentId") String studentId, @Param("taskId") String taskId, @Param("quizTemplateId") String quizTemplateId);
    
    @Query("SELECT COUNT(s) > 0 FROM Score s WHERE s.studentId = :studentId AND s.taskId = :taskId AND s.exerciseTemplateId = :exerciseTemplateId")
    boolean existsExerciseScore(@Param("studentId") String studentId, @Param("taskId") String taskId, @Param("exerciseTemplateId") String exerciseTemplateId);
} 