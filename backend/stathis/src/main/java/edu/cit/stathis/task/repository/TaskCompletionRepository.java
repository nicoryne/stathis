package edu.cit.stathis.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.task.entity.TaskCompletion;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, String> {
    TaskCompletion findByStudentIdAndTaskId(String studentId, String taskId);
    List<TaskCompletion> findByStudentId(String studentId);
    List<TaskCompletion> findByTaskId(String taskId);
    TaskCompletion findByPhysicalId(String physicalId);
    boolean existsByPhysicalId(String physicalId);
} 