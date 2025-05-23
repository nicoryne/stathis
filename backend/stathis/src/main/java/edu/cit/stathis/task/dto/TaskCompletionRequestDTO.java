package edu.cit.stathis.task.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TaskCompletionRequestDTO {
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Task ID is required")
    private String taskId;
} 