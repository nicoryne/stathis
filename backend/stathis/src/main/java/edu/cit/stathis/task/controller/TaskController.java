package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import edu.cit.stathis.task.service.TaskService;
import edu.cit.stathis.task.dto.TaskBodyDTO;
import edu.cit.stathis.task.dto.TaskResponseDTO;
import edu.cit.stathis.task.entity.Task;
import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task", description = "Create a new task")
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody TaskBodyDTO taskBodyDTO) {
        Task task = taskService.createTask(taskBodyDTO);
        return ResponseEntity.ok(taskService.getTaskResponseDTO(task.getPhysicalId()));
    }

    @GetMapping("/{physicalId}")
    @Operation(summary = "Get a task by its physical ID", description = "Get a task by its physical ID")
    public ResponseEntity<TaskResponseDTO> getTask(@PathVariable String physicalId) {
        Task task = taskService.getTaskByPhysicalId(physicalId);
        return ResponseEntity.ok(taskService.getTaskResponseDTO(task.getPhysicalId()));
    }

    @GetMapping("/classroom/{classroomPhysicalId}")
    @Operation(summary = "Get tasks by classroom physical ID", description = "Get tasks by classroom physical ID")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByClassroomPhysicalId(@PathVariable String classroomPhysicalId) {
        List<Task> tasks = taskService.getTasksByClassroomPhysicalId(classroomPhysicalId);
        return ResponseEntity.ok(tasks.stream()
            .map(task -> taskService.getTaskResponseDTO(task.getPhysicalId()))
            .collect(Collectors.toList()));
    }

    @PutMapping("/{physicalId}")
    @Operation(summary = "Update a task by its physical ID", description = "Update a task by its physical ID")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable String physicalId, @RequestBody TaskBodyDTO taskBodyDTO) {
        Task task = taskService.updateTaskByPhysicalId(physicalId, taskBodyDTO);
        return ResponseEntity.ok(taskService.getTaskResponseDTO(task.getPhysicalId()));
    }

    @DeleteMapping("/{physicalId}")
    @Operation(summary = "Delete a task by its physical ID", description = "Delete a task by its physical ID")
    public ResponseEntity<Void> deleteTask(@PathVariable String physicalId) {
        taskService.deleteTaskByPhysicalId(physicalId);
        return ResponseEntity.noContent().build();
    }
}
