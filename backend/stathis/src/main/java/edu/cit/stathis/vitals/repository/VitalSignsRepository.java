package edu.cit.stathis.vitals.repository;

import edu.cit.stathis.vitals.entity.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {
    List<VitalSigns> findByClassroomIdAndTaskId(Long classroomId, Long taskId);
    List<VitalSigns> findByStudentIdAndTaskId(Long studentId, Long taskId);
    List<VitalSigns> findByStudentIdAndTaskIdAndIsPreActivity(Long studentId, Long taskId, Boolean isPreActivity);
    List<VitalSigns> findByStudentIdAndTaskIdAndIsPostActivity(Long studentId, Long taskId, Boolean isPostActivity);
} 