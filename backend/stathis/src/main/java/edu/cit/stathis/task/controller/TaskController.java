package edu.cit.stathis.task.controller;

import edu.cit.stathis.task.dto.TaskBodyDTO;
import edu.cit.stathis.task.entity.Task;
import edu.cit.stathis.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new task", description = "Create a new task by its physical ID")
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskBodyDTO taskBodyDTO) {
        return ResponseEntity.ok(taskService.createTask(taskBodyDTO));
    }

    @PutMapping("/{physicalId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update a task", description = "Update a task by its physical ID")
    public ResponseEntity<Task> updateTask(
            @PathVariable String physicalId,
            @Valid @RequestBody TaskBodyDTO taskBodyDTO) {
        return ResponseEntity.ok(taskService.updateTask(physicalId, taskBodyDTO));
    }

    @DeleteMapping("/{physicalId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete a task", description = "Delete a task by its physical ID")
    public ResponseEntity<Void> deleteTask(@PathVariable String physicalId) {
        taskService.deleteTask(physicalId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{physicalId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get a task by its physical ID", description = "Get a task by its physical ID by its physical ID")
    public ResponseEntity<Task> getTask(@PathVariable String physicalId) {
        return ResponseEntity.of(taskService.getTaskByPhysicalId(physicalId));
    }

    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get tasks by classroom ID", description = "Get tasks by classroom ID by its physical ID")
    public ResponseEntity<List<Task>> getTasksByClassroom(@PathVariable String classroomId) {
        return ResponseEntity.ok(taskService.getTasksByClassroom(classroomId));
    }

    @GetMapping("/classroom/{classroomId}/active")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get active tasks by classroom ID", description = "Get active tasks by classroom ID by its physical ID")
    public ResponseEntity<List<Task>> getActiveTasksByClassroom(@PathVariable String classroomId) {
        return ResponseEntity.ok(taskService.getActiveTasksByClassroom(classroomId));
    }

    @GetMapping("/classroom/{classroomId}/started")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "Get started tasks by classroom ID", description = "Get started tasks by classroom ID by its physical ID")
    public ResponseEntity<List<Task>> getStartedTasksByClassroom(@PathVariable String classroomId) {
        return ResponseEntity.ok(taskService.getStartedTasksByClassroom(classroomId));
    }

    @PostMapping("/{physicalId}/start")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Start a task", description = "Start a task by its physical ID")
    public ResponseEntity<Void> startTask(@PathVariable String physicalId) {
        taskService.startTask(physicalId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{physicalId}/deactivate")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Deactivate a task", description = "Deactivate a task by its physical ID")
    public ResponseEntity<Void> deactivateTask(@PathVariable String physicalId) {
        taskService.deactivateTask(physicalId);
        return ResponseEntity.ok().build();
    }
}
