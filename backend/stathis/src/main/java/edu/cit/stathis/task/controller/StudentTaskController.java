package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import edu.cit.stathis.task.service.StudentTaskService;
import edu.cit.stathis.task.dto.StudentTaskResponseDTO;
import edu.cit.stathis.task.entity.Score;
import java.util.List;

@RestController
@RequestMapping("/student/tasks")
public class StudentTaskController {
    @Autowired
    private StudentTaskService studentTaskService;

    @GetMapping
    public ResponseEntity<List<StudentTaskResponseDTO>> getStudentTasks(
            @RequestHeader("X-Student-ID") String studentId) {
        return ResponseEntity.ok(studentTaskService.getStudentTasks(studentId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<StudentTaskResponseDTO> getStudentTask(
            @PathVariable String taskId,
            @RequestHeader("X-Student-ID") String studentId) {
        return ResponseEntity.ok(studentTaskService.getStudentTask(taskId, studentId));
    }

    @PostMapping("/{taskId}/quiz/{quizTemplateId}/score")
    public ResponseEntity<Score> submitQuizScore(
            @PathVariable String taskId,
            @PathVariable String quizTemplateId,
            @RequestHeader("X-Student-ID") String studentId,
            @RequestBody int score) {
        return ResponseEntity.ok(studentTaskService.submitQuizScore(
            studentId, taskId, quizTemplateId, score));
    }
} 