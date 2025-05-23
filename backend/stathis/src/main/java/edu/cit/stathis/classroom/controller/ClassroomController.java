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
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<ClassroomResponseDTO> createClassroom(@RequestBody ClassroomBodyDTO classroomDTO) {
        Classroom classroom = classroomService.createClassroom(classroomDTO, classroomDTO.getTeacherId());
        ClassroomResponseDTO response = classroomService.buildClassroomResponse(classroom);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{physicalId}")
    public ResponseEntity<ClassroomResponseDTO> getClassroomById(@PathVariable String physicalId) {
        Classroom classroom = classroomService.getClassroomById(physicalId);
        ClassroomResponseDTO response = classroomService.buildClassroomResponse(classroom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{physicalId}")
    public ResponseEntity<ClassroomResponseDTO> updateClassroomById(
            @PathVariable String physicalId, 
            @RequestBody ClassroomBodyDTO classroomDTO) {
        Classroom classroom = classroomService.updateClassroomById(physicalId, classroomDTO);
        ClassroomResponseDTO response = classroomService.buildClassroomResponse(classroom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{physicalId}")
    public ResponseEntity<Void> deleteClassroomById(@PathVariable String physicalId) {
        classroomService.deleteClassroomById(physicalId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ClassroomResponseDTO>> getClassroomsByTeacherId(@PathVariable String teacherId) {
        List<Classroom> classrooms = classroomService.getClassroomsByTeacherId(teacherId);
        List<ClassroomResponseDTO> response = classrooms.stream()
            .map(classroomService::buildClassroomResponse)
            .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ClassroomResponseDTO>> getClassroomsByStudentId(@PathVariable String studentId) {
        List<Classroom> classrooms = classroomService.getClassroomsByStudentId(studentId);
        List<ClassroomResponseDTO> response = classrooms.stream()
            .map(classroomService::buildClassroomResponse)
            .collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{classroomPhysicalId}/enroll")
    public ResponseEntity<Void> enrollStudentInClassroom(@PathVariable String classroomPhysicalId, @RequestBody String studentId) {
        classroomService.enrollStudentInClassroom(classroomPhysicalId, studentId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{classroomPhysicalId}/students")
    public ResponseEntity<List<StudentListResponseDTO>> getStudentListByClassroomPhysicalId(@PathVariable String classroomPhysicalId) {
        List<StudentListResponseDTO> students = classroomService.getStudentListByClassroomPhysicalId(classroomPhysicalId);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @PostMapping("/{classroomPhysicalId}/students/{studentId}/verify")
    public ResponseEntity<Void> verifyStudentStatus(@PathVariable String classroomPhysicalId, @PathVariable String studentId) {
        classroomService.verifyStudentStatus(classroomPhysicalId, studentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{classroomPhysicalId}/deactivate")
    public ResponseEntity<Void> deactivateClassroom(@PathVariable String classroomPhysicalId) {
        classroomService.deactivateClassroom(classroomPhysicalId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{classroomPhysicalId}/activate")
    public ResponseEntity<Void> activateClassroom(@PathVariable String classroomPhysicalId) {
        classroomService.activateClassroom(classroomPhysicalId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
