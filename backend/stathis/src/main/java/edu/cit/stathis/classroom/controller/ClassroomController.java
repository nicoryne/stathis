package edu.cit.stathis.classroom.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

import edu.cit.stathis.classroom.service.ClassroomService;
import edu.cit.stathis.classroom.entity.Classroom;
import edu.cit.stathis.classroom.dto.ClassroomBodyDTO;
import edu.cit.stathis.classroom.dto.ClassroomResponseDTO;
import edu.cit.stathis.classroom.dto.StudentListResponseDTO;

@RestController
@RequestMapping("/api/classrooms")
@Tag(name = "Classrooms", description = "Endpoints related to classrooms")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @PostMapping
    @Operation(summary = "Create a new classroom", description = "Create a new classroom")
    public ResponseEntity<ClassroomResponseDTO> createClassroom(@RequestBody ClassroomBodyDTO classroomDTO) {
        Classroom classroom = classroomService.createClassroom(classroomDTO);
        ClassroomResponseDTO response = classroomService.buildClassroomResponse(classroom);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{physicalId}")
    @Operation(summary = "Get a classroom by its physical ID", description = "Get a classroom by its physical ID")
    public ResponseEntity<ClassroomResponseDTO> getClassroomById(@PathVariable String physicalId) {
        Classroom classroom = classroomService.getClassroomById(physicalId);
        ClassroomResponseDTO response = classroomService.buildClassroomResponse(classroom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{physicalId}")
    @Operation(summary = "Update a classroom by its physical ID", description = "Update a classroom by its physical ID")
    public ResponseEntity<ClassroomResponseDTO> updateClassroomById(
            @PathVariable String physicalId, 
            @RequestBody ClassroomBodyDTO classroomDTO) {
        Classroom classroom = classroomService.updateClassroomById(physicalId, classroomDTO);
        ClassroomResponseDTO response = classroomService.buildClassroomResponse(classroom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{physicalId}")
    @Operation(summary = "Delete a classroom by its physical ID", description = "Delete a classroom by its physical ID")
    public ResponseEntity<Void> deleteClassroomById(@PathVariable String physicalId) {
        classroomService.deleteClassroomById(physicalId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/teacher")
    @Operation(summary = "Get classrooms by current teacher", description = "Get classrooms by current teacher")
    public ResponseEntity<List<ClassroomResponseDTO>> getClassroomsByCurrentTeacher() {
        List<Classroom> classrooms = classroomService.getClassroomsByCurrentTeacher();
        List<ClassroomResponseDTO> response = classrooms.stream()
            .map(classroomService::buildClassroomResponse)
            .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/student")
    @Operation(summary = "Get classrooms by current student", description = "Get classrooms by current student")
    public ResponseEntity<List<ClassroomResponseDTO>> getClassroomsByCurrentStudent() {
        List<Classroom> classrooms = classroomService.getClassroomsByCurrentStudent();
        List<ClassroomResponseDTO> response = classrooms.stream()
            .map(classroomService::buildClassroomResponse)
            .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/enroll")
    @Operation(summary = "Enroll a student in a classroom", description = "Enroll a student in a classroom")
    public ResponseEntity<Void> enrollStudentInClassroom(@RequestBody Map<String, String> body) {
        String classroomCode = body.get("classroomCode");
        classroomService.enrollStudentInClassroom(classroomCode);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{classroomPhysicalId}/students")
    @Operation(summary = "Get students in a classroom", description = "Get students in a classroom")
    public ResponseEntity<List<StudentListResponseDTO>> getStudentListByClassroomPhysicalId(@PathVariable String classroomPhysicalId) {
        List<StudentListResponseDTO> students = classroomService.getStudentListByClassroomPhysicalId(classroomPhysicalId);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @PostMapping("/{classroomPhysicalId}/students/{studentId}/verify")
    @Operation(summary = "Verify a student's status in a classroom", description = "Verify a student's status in a classroom")
    public ResponseEntity<Void> verifyStudentStatus(@PathVariable String classroomPhysicalId, @PathVariable String studentId) {
        classroomService.verifyStudentStatus(classroomPhysicalId, studentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{classroomPhysicalId}/deactivate")
    @Operation(summary = "Deactivate a classroom", description = "Deactivate a classroom")
    public ResponseEntity<Void> deactivateClassroom(@PathVariable String classroomPhysicalId) {
        classroomService.deactivateClassroom(classroomPhysicalId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{classroomPhysicalId}/activate")
    @Operation(summary = "Activate a classroom", description = "Activate a classroom")
    public ResponseEntity<Void> activateClassroom(@PathVariable String classroomPhysicalId) {
        classroomService.activateClassroom(classroomPhysicalId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
