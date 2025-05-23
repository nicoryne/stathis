package edu.cit.stathis.task.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskCompletionResponseDTO {
    private boolean success;
    private String message;
    private String studentId;
    private String taskId;
} 