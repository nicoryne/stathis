package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import edu.cit.stathis.task.service.StudentTaskService;
import edu.cit.stathis.auth.service.PhysicalIdService;
import edu.cit.stathis.task.dto.StudentTaskResponseDTO;
import edu.cit.stathis.task.dto.TaskProgressDTO;
import edu.cit.stathis.task.entity.Score;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/student/tasks")
public class StudentTaskController {
    @Autowired
    private StudentTaskService studentTaskService;

    @Autowired
    private PhysicalIdService physicalIdService;

    @GetMapping("/classroom/{classroomPhysicalId}")
    @Operation(summary = "Get student tasks for a classroom", description = "Get student tasks for a classroom")
    public ResponseEntity<List<StudentTaskResponseDTO>> getStudentTasksForClassroom(@PathVariable String classroomPhysicalId) {
        String studentId = physicalIdService.getCurrentUserPhysicalId();
        return ResponseEntity.ok(studentTaskService.getStudentTasks(classroomPhysicalId, studentId));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get student task", description = "Get student task")
    public ResponseEntity<StudentTaskResponseDTO> getStudentTask(@PathVariable String taskId) {
        String studentId = physicalIdService.getCurrentUserPhysicalId();
        return ResponseEntity.ok(studentTaskService.getStudentTask(taskId, studentId));
    }

    @GetMapping("/{taskId}/progress")
    @Operation(summary = "Get task progress", description = "Get task progress for a student")
    public ResponseEntity<TaskProgressDTO> getTaskProgress(@PathVariable String taskId) {
        String studentId = physicalIdService.getCurrentUserPhysicalId();
        return ResponseEntity.ok(studentTaskService.getTaskProgress(taskId, studentId));
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

    @PostMapping("/{taskId}/lesson/{lessonTemplateId}/complete")
    @Operation(summary = "Mark lesson as completed", description = "Mark a lesson as completed")
    public ResponseEntity<Void> completeLesson(
            @PathVariable String taskId,
            @PathVariable String lessonTemplateId) {
        String studentId = physicalIdService.getCurrentUserPhysicalId();
        studentTaskService.completeLesson(studentId, taskId, lessonTemplateId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/exercise/{exerciseTemplateId}/complete")
    @Operation(summary = "Mark exercise as completed", description = "Mark an exercise as completed")
    public ResponseEntity<Void> completeExercise(
            @PathVariable String taskId,
            @PathVariable String exerciseTemplateId) {
        String studentId = physicalIdService.getCurrentUserPhysicalId();
        studentTaskService.completeExercise(studentId, taskId, exerciseTemplateId);
        return ResponseEntity.ok().build();
    }
} 