package edu.cit.stathis.task.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizTemplateBodyDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Instruction is required")
    @Size(min = 3, max = 1000, message = "Instruction must be between 3 and 1000 characters")
    private String instruction;

    @NotBlank(message = "Max score is required")
    @Min(value = 0, message = "Max score must be greater than 0")
    @Max(value = 100, message = "Max score must be less than 100")
    private int maxScore;

    private Map<String, Object> content;
}
