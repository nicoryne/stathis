package edu.cit.stathis.task.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonTemplateBodyDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 1000, message = "Description must be between 3 and 1000 characters")
    private String description;

    @NotBlank(message = "Content is required")
    @Size(min = 3, max = 1000, message = "Content must be between 3 and 1000 characters")
    private Map<String, Object> content;
}
