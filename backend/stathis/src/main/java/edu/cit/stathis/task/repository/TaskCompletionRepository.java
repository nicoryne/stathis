package edu.cit.stathis.task.repository;

import edu.cit.stathis.task.entity.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, String> {
    
    Optional<TaskCompletion> findByStudentIdAndTaskId(String studentId, String taskId);
    
    List<TaskCompletion> findByStudentId(String studentId);
    
    List<TaskCompletion> findByTaskId(String taskId);
    
    @Query("SELECT tc FROM TaskCompletion tc WHERE tc.taskId = :taskId AND tc.isFullyCompleted = true")
    List<TaskCompletion> findCompletedByTaskId(@Param("taskId") String taskId);
    
    @Query("SELECT tc FROM TaskCompletion tc WHERE tc.taskId = :taskId AND tc.submittedForReview = true")
    List<TaskCompletion> findSubmittedForReviewByTaskId(@Param("taskId") String taskId);
    
    @Query("SELECT COUNT(tc) FROM TaskCompletion tc WHERE tc.taskId = :taskId AND tc.isFullyCompleted = true")
    long countCompletedByTaskId(@Param("taskId") String taskId);
    
    @Query("SELECT COUNT(tc) FROM TaskCompletion tc WHERE tc.taskId = :taskId AND tc.submittedForReview = true")
    long countSubmittedForReviewByTaskId(@Param("taskId") String taskId);
    
    boolean existsByStudentIdAndTaskId(String studentId, String taskId);
} 