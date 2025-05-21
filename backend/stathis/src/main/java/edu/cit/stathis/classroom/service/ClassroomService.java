package edu.cit.stathis.classroom.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import edu.cit.stathis.classroom.repository.ClassroomRepository;
import edu.cit.stathis.classroom.dto.ClassroomBodyDTO;
import edu.cit.stathis.classroom.dto.ClassroomResponseDTO;
import edu.cit.stathis.classroom.entity.Classroom;
import edu.cit.stathis.auth.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.List;

@Service
public class ClassroomService {
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Classroom createClassroom(ClassroomBodyDTO createClassroomDTO, String teacherId) {
        Classroom classroom = new Classroom();
        classroom.setPhysicalId(provideUniquePhysicalId());
        classroom.setName(createClassroomDTO.getName());
        classroom.setDescription(createClassroomDTO.getDescription());
        classroom.setTeacherId(teacherId);
        return classroomRepository.save(classroom);
    }

    @Transactional
    public Classroom updateClassroomById(String physicalId, ClassroomBodyDTO classroomDTO) {
        Classroom classroom = classroomRepository.findByPhysicalId(physicalId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (!classroomDTO.getTeacherId().equals(classroom.getTeacherId())) {
            throw new RuntimeException("You are not authorized to update this classroom");
        }

        classroom.setName(classroomDTO.getName());
        classroom.setDescription(classroomDTO.getDescription());
        return classroomRepository.save(classroom);
    }

    public Classroom getClassroomById(String physicalId) {
        return classroomRepository.findByPhysicalId(physicalId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
    }

    @Transactional
    public void deleteClassroomById(String physicalId) {
        Classroom classroom = classroomRepository.findByPhysicalId(physicalId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        classroomRepository.delete(classroom);
    }

    public List<Classroom> getClassroomsByTeacherId(String teacherId) {
        return classroomRepository.findByTeacherId(teacherId);
    }

    public List<Classroom> getClassroomsByStudentId(String studentId) {
        return classroomRepository.findByClassroomStudents_Student_UserId(UUID.fromString(studentId));
    }

    public ClassroomResponseDTO buildClassroomResponse(Classroom classroom) {
        return ClassroomResponseDTO.builder()
            .physicalId(classroom.getPhysicalId())
            .name(classroom.getName())
            .description(classroom.getDescription())
            .createdAt(classroom.getCreatedAt().toString())
            .updatedAt(classroom.getUpdatedAt().toString())
            .isActive(classroom.isActive())
            .teacherName(getTeacherName(classroom.getTeacherId()))
            .studentCount(classroom.getClassroomStudents().size())
            .build();
    }

    public String getTeacherName(String teacherId) {
        return userService.findUserProfileByUserId(UUID.fromString(teacherId)).getFirstName() + " " + userService.findUserProfileByUserId(UUID.fromString(teacherId)).getLastName();
    }

    private String generatePhysicalId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%04d", random.nextInt(10000));
        String thirdPart = String.format("%03d", random.nextInt(1000));
        return String.format("%s-%s-%s", year, secondPart, thirdPart);
    }
    
    private String provideUniquePhysicalId() {
        String generatedPhysicalId;
        do {
            generatedPhysicalId = generatePhysicalId();
        } while (classroomRepository.existsByPhysicalId(generatedPhysicalId));
        return generatedPhysicalId;
    }
}
