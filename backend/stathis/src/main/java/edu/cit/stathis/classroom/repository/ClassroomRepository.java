package edu.cit.stathis.classroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.classroom.entity.Classroom;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
    List<Classroom> findByTeacherId(String teacherId);
    boolean existsByPhysicalId(String physicalId);
    Optional<Classroom> findByPhysicalId(String physicalId);
    List<Classroom> findByClassroomStudents_Student_UserId(UUID studentId);
}
