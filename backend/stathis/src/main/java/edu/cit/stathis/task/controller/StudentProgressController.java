package edu.cit.stathis.task.controller;

import edu.cit.stathis.task.dto.StudentProgressDTO;
import edu.cit.stathis.task.service.StudentProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-progress")
@Tag(name = "Student Progress", description = "Aggregated progress of a student across tasks")
public class StudentProgressController {

    @Autowired
    private StudentProgressService studentProgressService;

    @GetMapping("/{studentId}")
    @Operation(summary = "Get student progress across tasks", description = "Returns aggregated task progress for a student, optionally filtered by classroom")
    public ResponseEntity<List<StudentProgressDTO>> getStudentProgress(
            @PathVariable String studentId,
            @RequestParam(required = false) String classroomId
    ) {
        List<StudentProgressDTO> result = studentProgressService.getStudentProgress(studentId, classroomId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}


