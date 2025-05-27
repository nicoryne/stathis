package edu.cit.stathis.vitals.repository;

import edu.cit.stathis.vitals.entity.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {
    Optional<VitalSigns> findByPhysicalId(String physicalId);
    List<VitalSigns> findByClassroomIdAndTaskId(String classroomId, String taskId);
    List<VitalSigns> findByStudentIdAndTaskId(String studentId, String taskId);
    List<VitalSigns> findByStudentIdAndTaskIdAndIsPreActivity(String studentId, String taskId, Boolean isPreActivity);
    List<VitalSigns> findByStudentIdAndTaskIdAndIsPostActivity(String studentId, String taskId, Boolean isPostActivity);
} 