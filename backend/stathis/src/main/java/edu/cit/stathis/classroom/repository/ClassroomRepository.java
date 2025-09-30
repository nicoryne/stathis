package edu.cit.stathis.classroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.cit.stathis.classroom.entity.Classroom;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, String> {
    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.classroomStudents WHERE c.physicalId = :physicalId")
    Optional<Classroom> findByPhysicalId(@Param("physicalId") String physicalId);
    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.classroomStudents WHERE c.teacherId = :teacherId")
    List<Classroom> findByTeacherId(@Param("teacherId") String teacherId);
    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.classroomStudents cs WHERE cs.student.user.physicalId = :studentPhysicalId")
    List<Classroom> findByClassroomStudents_Student_User_PhysicalId(@Param("studentPhysicalId") String studentPhysicalId);
    @Query("SELECT c FROM Classroom c LEFT JOIN FETCH c.classroomStudents WHERE c.classroomCode = :classroomCode")
    Optional<Classroom> findByClassroomCode(@Param("classroomCode") String classroomCode);
    boolean existsByPhysicalId(String physicalId);
}
