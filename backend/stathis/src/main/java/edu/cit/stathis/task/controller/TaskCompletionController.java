package edu.cit.stathis.task.controller;

import edu.cit.stathis.task.dto.TaskProgressDTO;
import edu.cit.stathis.task.entity.TaskCompletion;
import edu.cit.stathis.task.service.TaskCompletionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/task-completions")
@RequiredArgsConstructor
public class TaskCompletionController {

    private final TaskCompletionService taskCompletionService;

    @PostMapping("/{taskId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Create a new task completion", description = "Create a new task completion")
    public ResponseEntity<TaskCompletion> createTaskCompletion(
            @PathVariable String taskId,
            @RequestParam String studentId) {
        return ResponseEntity.ok(taskCompletionService.createTaskCompletion(studentId, taskId));
    }

    @PutMapping("/{taskId}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update task progress", description = "Update task progress")
    public ResponseEntity<TaskCompletion> updateTaskProgress(
            @PathVariable String taskId,
            @RequestParam String studentId,
            @Valid @RequestBody TaskProgressDTO progressDTO) {
        return ResponseEntity.ok(taskCompletionService.updateTaskProgress(studentId, taskId, progressDTO));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get task completion", description = "Get task completion")
    public ResponseEntity<TaskCompletion> getTaskCompletion(
            @PathVariable String taskId,
            @RequestParam String studentId) {
        return taskCompletionService.getTaskCompletion(studentId, taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get task completions by student ID", description = "Get task completions by student ID")
    public ResponseEntity<List<TaskCompletion>> getTaskCompletionsByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(taskCompletionService.getTaskCompletionsByStudent(studentId));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get task completions by task ID", description = "Get task completions by task ID")
    public ResponseEntity<List<TaskCompletion>> getTaskCompletionsByTask(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCompletionService.getTaskCompletionsByTask(taskId));
    }

    @GetMapping("/task/{taskId}/completed")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get completed tasks", description = "Get completed tasks")
    public ResponseEntity<List<TaskCompletion>> getCompletedTasks(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCompletionService.getCompletedTasks(taskId));
    }

    @GetMapping("/task/{taskId}/submitted")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get submitted for review tasks", description = "Get submitted for review tasks")
    public ResponseEntity<List<TaskCompletion>> getSubmittedForReviewTasks(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCompletionService.getSubmittedForReviewTasks(taskId));
    }

    @GetMapping("/task/{taskId}/completed/count")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Count completed tasks", description = "Count completed tasks")
    public ResponseEntity<Long> countCompletedTasks(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCompletionService.countCompletedTasks(taskId));
    }

    @GetMapping("/task/{taskId}/submitted/count")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Count submitted for review", description = "Count submitted for review")
    public ResponseEntity<Long> countSubmittedForReviewTasks(@PathVariable String taskId) {
        return ResponseEntity.ok(taskCompletionService.countSubmittedForReviewTasks(taskId));
    }

    @PostMapping("/{taskId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit for review", description = "Submit for review")
    public ResponseEntity<Void> submitForReview(
            @PathVariable String taskId,
            @RequestParam String studentId) {
        taskCompletionService.submitForReview(studentId, taskId);
        return ResponseEntity.ok().build();
    }
} 