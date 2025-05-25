package edu.cit.stathis.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskBodyDTO {
    @NotBlank(message = "Task name is required")
    @Size(min = 3, max = 100, message = "Task name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Submission date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}([+-]\\d{2}:\\d{2}|Z)$", 
        message = "Invalid submission date format. Use ISO-8601 format (e.g., 2024-03-25T10:30:00+08:00)")
    private String submissionDate;

    @NotBlank(message = "Closing date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}([+-]\\d{2}:\\d{2}|Z)$", 
        message = "Invalid closing date format. Use ISO-8601 format (e.g., 2024-03-25T10:30:00+08:00)")
    private String closingDate;

    private String imageUrl;

    @NotBlank(message = "Classroom ID is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Invalid classroom ID format")
    private String classroomPhysicalId;

    @Pattern(regexp = "^EXERCISE-[A-Z0-9-]+$", message = "Invalid exercise template ID format")
    private String exerciseTemplateId;

    @Pattern(regexp = "^LESSON-[A-Z0-9-]+$", message = "Invalid lesson template ID format")
    private String lessonTemplateId;

    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Invalid quiz template ID format")
    private String quizTemplateId;

    private Integer maxAttempts;

    public boolean hasAtLeastOneTemplate() {
        return exerciseTemplateId != null || lessonTemplateId != null || quizTemplateId != null;
    }
}
