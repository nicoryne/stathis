package edu.cit.stathis.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskBodyDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Submission date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Submission date must be in YYYY-MM-DD format")
    private String submissionDate;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Closing date must be in YYYY-MM-DD format")
    private String closingDate;

    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|bmp|tiff|tif|ico|webp|svg|svgz|mp4|webm|ogg|mp3|wav|flac|aac|m4a|wma|ape|aiff|au|amr|3gp|mov|avi|wmv|flv|m4v|m4p|m4b|m4r|m4v|m4p|m4b|m4r)$", message = "Invalid image URL format")
    private String imageUrl;

    @NotBlank(message = "Classroom ID is required")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
             message = "Invalid classroom ID format")
    private String classroomPhysicalId;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
             message = "Invalid exercise template ID format")
    private String exerciseTemplateId;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
             message = "Invalid lesson template ID format")
    private String lessonTemplateId;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
             message = "Invalid quiz template ID format")
    private String quizTemplateId;
}
