package edu.cit.stathis.task.repository;

import edu.cit.stathis.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    Optional<Task> findByPhysicalId(String physicalId);
    
    List<Task> findByClassroomPhysicalId(String classroomPhysicalId);
    
    @Query("SELECT t FROM Task t WHERE t.classroomPhysicalId = :classroomId AND t.isActive = true")
    List<Task> findActiveTasksByClassroom(@Param("classroomId") String classroomId);
    
    @Query("SELECT t FROM Task t WHERE t.classroomPhysicalId = :classroomId AND t.isActive = true AND t.isStarted = true")
    List<Task> findStartedTasksByClassroom(@Param("classroomId") String classroomId);
    
    boolean existsByPhysicalId(String physicalId);
    
    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.physicalId = :physicalId AND t.classroomPhysicalId = :classroomId")
    boolean existsByPhysicalIdAndClassroomId(@Param("physicalId") String physicalId, @Param("classroomId") String classroomId);

    List<Task> findByClassroomPhysicalIdAndIsActiveTrue(String classroomPhysicalId);
    List<Task> findByClassroomPhysicalIdAndIsStartedTrue(String classroomPhysicalId);
}
