package edu.cit.stathis.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.task.entity.Badge;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    List<Badge> findByStudentId(String studentId);
    List<Badge> findByTaskId(String taskId);
    Badge findByPhysicalId(String physicalId);
    boolean existsByPhysicalId(String physicalId);
    boolean existsByStudentIdAndTaskIdAndBadgeType(String studentId, String taskId, String badgeType);
} 