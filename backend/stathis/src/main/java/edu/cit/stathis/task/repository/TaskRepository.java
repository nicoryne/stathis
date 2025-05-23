package edu.cit.stathis.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.task.entity.Task;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Task findByName(String name);
    List<Task> findByClassroomPhysicalId(String classroomPhysicalId);
    Task findByPhysicalId(String physicalId);
}
