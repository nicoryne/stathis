package edu.cit.stathis.classroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.cit.stathis.classroom.entity.Classroom;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, String> {
    Optional<Classroom> findByPhysicalId(String physicalId);
    List<Classroom> findByTeacherId(String teacherId);
    List<Classroom> findByClassroomStudents_Student_User_PhysicalId(String studentPhysicalId);
    Optional<Classroom> findByClassroomCode(String classroomCode);
    boolean existsByPhysicalId(String physicalId);
}
