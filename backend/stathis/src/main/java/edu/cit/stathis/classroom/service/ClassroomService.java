package edu.cit.stathis.classroom.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import edu.cit.stathis.classroom.repository.ClassroomRepository;
import edu.cit.stathis.classroom.dto.ClassroomBodyDTO;
import edu.cit.stathis.classroom.dto.ClassroomResponseDTO;
import edu.cit.stathis.classroom.dto.StudentListResponseDTO;
import edu.cit.stathis.classroom.entity.Classroom;
import edu.cit.stathis.classroom.entity.ClassroomStudents;
import edu.cit.stathis.auth.service.UserService;
import edu.cit.stathis.auth.service.PhysicalIdService;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.List;
import java.util.Base64;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@Service
public class ClassroomService {
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PhysicalIdService physicalIdService;

    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public Classroom createClassroom(ClassroomBodyDTO createClassroomDTO) {
        Classroom classroom = new Classroom();
        classroom.setPhysicalId(provideUniquePhysicalId());
        classroom.setName(createClassroomDTO.getName());
        classroom.setDescription(createClassroomDTO.getDescription());
        classroom.setTeacherId(physicalIdService.getCurrentUserPhysicalId());
        classroom.setClassroomCode(generateClassroomCode(classroom));
        OffsetDateTime now = OffsetDateTime.now();
        classroom.setCreatedAt(now);
        classroom.setUpdatedAt(now);
        return classroomRepository.save(classroom);
    }

    @Transactional
    public Classroom updateClassroomById(String physicalId, ClassroomBodyDTO classroomDTO) {
        Classroom classroom = classroomRepository.findByPhysicalId(physicalId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (!physicalIdService.getCurrentUserPhysicalId().equals(classroom.getTeacherId())) {
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

    public List<Classroom> getClassroomsByCurrentTeacher() {
        return classroomRepository.findByTeacherId(physicalIdService.getCurrentUserPhysicalId());
    }

    public List<Classroom> getClassroomsByCurrentStudent() {
        return classroomRepository.findByClassroomStudents_Student_User_PhysicalId(
            physicalIdService.getCurrentUserPhysicalId());
    }

    public String generateClassroomCode(Classroom classroom) {
        String classroomCode = Base64.getEncoder().encodeToString(classroom.getPhysicalId().getBytes());
        classroom.setClassroomCode(classroomCode);
        classroomRepository.save(classroom);
        return classroomCode;
    }

    @PreAuthorize("hasRole('STUDENT')")
    public void enrollStudentInClassroom(String classroomCode) {
        String studentPhysicalId = physicalIdService.getCurrentUserPhysicalId();

        Classroom classroom = classroomRepository.findByClassroomCode(classroomCode)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        
        if (!classroom.isActive()) {
            throw new RuntimeException("Classroom is not active");
        }
        
        boolean alreadyEnrolled = classroom.getClassroomStudents().stream()
            .anyMatch(cs -> cs.getStudent().getUser().getPhysicalId().equals(studentPhysicalId));
        if (alreadyEnrolled) {
            throw new RuntimeException("Student is already enrolled");
        }
        
        try {
            var studentProfile = userService.findUserProfileByPhysicalId(studentPhysicalId);
            ClassroomStudents classroomStudents = new ClassroomStudents();
            classroomStudents.setPhysicalId(provideUniqueClassroomStudentId());
            classroomStudents.setClassroom(classroom);
            classroomStudents.setStudent(studentProfile);
            classroomStudents.setCreatedAt(OffsetDateTime.now());
            classroomStudents.setUpdatedAt(OffsetDateTime.now());
            classroomStudents.setVerified(false);
            classroom.getClassroomStudents().add(classroomStudents);
            classroomRepository.save(classroom);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<StudentListResponseDTO> getStudentListByClassroomPhysicalId(String classroomPhysicalId) {
        Classroom classroom = classroomRepository.findByPhysicalId(classroomPhysicalId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        return classroom.getClassroomStudents().stream()
            .map(this::buildStudentListResponse)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public void verifyStudentStatus(String classroomPhysicalId, String studentId) {
        Classroom classroom = classroomRepository.findByPhysicalId(classroomPhysicalId)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        ClassroomStudents classroomStudents = classroom.getClassroomStudents().stream()
            .filter(cs -> cs.getStudent().getUser().getUserId().toString().equals(studentId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Student not found in classroom"));
        classroomStudents.setVerified(true);
        classroomRepository.save(classroom);
    }

    private StudentListResponseDTO buildStudentListResponse(ClassroomStudents classroomStudents) {
        return StudentListResponseDTO.builder()
            .physicalId(classroomStudents.getStudent().getUser().getPhysicalId())
            .firstName(classroomStudents.getStudent().getFirstName())
            .lastName(classroomStudents.getStudent().getLastName())
            .email(classroomStudents.getStudent().getUser().getEmail())
            .profilePictureUrl(classroomStudents.getStudent().getProfilePictureUrl())
            .joinedAt(classroomStudents.getCreatedAt().toString())
            .isVerified(classroomStudents.isVerified())
            .build();
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
            .classroomCode(classroom.getClassroomCode())
            .build();
    }

    public String getTeacherName(String teacherId) {
        return userService.findUserProfileByPhysicalId(teacherId).getFirstName() + " " + 
               userService.findUserProfileByPhysicalId(teacherId).getLastName();
    }

    private String generatePhysicalId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%03d", random.nextInt(1000));
        return String.format("ROOM-%s-%s", year, secondPart);
    }
    
    private String provideUniquePhysicalId() {
        String generatedPhysicalId;
        do {
            generatedPhysicalId = generatePhysicalId();
        } while (classroomRepository.existsByPhysicalId(generatedPhysicalId));
        return generatedPhysicalId;
    }

    private String provideUniqueClassroomStudentId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%03d", random.nextInt(1000));
        return String.format("CS-%s-%s", year, secondPart);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public void deactivateClassroom(String physicalId) {
        Classroom classroom = getClassroomById(physicalId);
        if (!classroom.isActive()) {
            throw new RuntimeException("Classroom is already deactivated");
        }
        classroom.setActive(false);
        classroomRepository.save(classroom);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public void activateClassroom(String physicalId) {
        Classroom classroom = getClassroomById(physicalId);
        if (classroom.isActive()) {
            throw new RuntimeException("Classroom is already active");
        }
        classroom.setActive(true);
        classroomRepository.save(classroom);
    }

    public boolean isUserEnrolledInClassroom(String userPhysicalId, String classroomPhysicalId) {
        Classroom classroom = getClassroomById(classroomPhysicalId);
        return classroom.getClassroomStudents().stream()
            .anyMatch(cs -> cs.getStudent().getUser().getPhysicalId().equals(userPhysicalId)) ||
            classroom.getTeacherId().equals(userPhysicalId);
    }

    public boolean isUserEnrolledAndVerifiedInClassroom(String userPhysicalId, String classroomPhysicalId) {
        Classroom classroom = getClassroomById(classroomPhysicalId);
        return classroom.getClassroomStudents().stream()
            .anyMatch(cs -> cs.getStudent().getUser().getPhysicalId().equals(userPhysicalId) && cs.isVerified());
    }
}
