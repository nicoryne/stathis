package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import edu.cit.stathis.task.service.StudentTaskService;
import edu.cit.stathis.auth.service.PhysicalIdService;
import edu.cit.stathis.task.dto.StudentTaskResponseDTO;
import edu.cit.stathis.task.entity.Score;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/student/tasks")
public class StudentTaskController {
    @Autowired
    private StudentTaskService studentTaskService;

    @Autowired
    private PhysicalIdService physicalIdService;

    @GetMapping
    @Operation(summary = "Get student tasks", description = "Get student tasks")
    public ResponseEntity<List<StudentTaskResponseDTO>> getStudentTasks() {
        String studentId = physicalIdService.getCurrentUserPhysicalId();
        return ResponseEntity.ok(studentTaskService.getStudentTasks(studentId));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get student task", description = "Get student task")
    public ResponseEntity<StudentTaskResponseDTO> getStudentTask(@PathVariable String taskId) {
        String studentId = physicalIdService.getCurrentUserPhysicalId();
        return ResponseEntity.ok(studentTaskService.getStudentTask(taskId, studentId));
    }

    @PostMapping("/{taskId}/quiz/{quizTemplateId}/score")
    @Operation(summary = "Submit quiz score", description = "Submit quiz score")
    public ResponseEntity<Score> submitQuizScore(
            @PathVariable String taskId,
            @PathVariable String quizTemplateId,
            @RequestBody int score) {
        String studentId = physicalIdService.getCurrentUserPhysicalId();
        return ResponseEntity.ok(studentTaskService.submitQuizScore(
            studentId, taskId, quizTemplateId, score));
    }
} 