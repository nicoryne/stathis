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
public class ExerciseTemplateBodyDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Exercise type is required")
    @Pattern(regexp = "^(PUSH_UP|SIT_UP|JUMPING_JACK|TYPE1|TYPE2)$", 
             message = "Invalid exercise type. Must be one of: PUSH_UP, SIT_UP, JUMPING_JACK, TYPE1, TYPE2")
    private String exerciseType;

    @NotBlank(message = "Exercise difficulty is required")
    @Pattern(regexp = "^(BEGINNER|INTERMEDIATE|ADVANCED)$", 
             message = "Invalid exercise difficulty. Must be one of: BEGINNER, INTERMEDIATE, ADVANCED")
    private String exerciseDifficulty;

    @NotBlank(message = "Goal reps is required")
    @Pattern(regexp = "^[0-9]+$", 
             message = "Goal reps must be a number")
    private String goalReps;

    @NotBlank(message = "Goal accuracy is required")
    @Pattern(regexp = "^[0-9]+$", 
             message = "Goal accuracy must be a number")
    private String goalAccuracy;

    @NotBlank(message = "Goal time is required")
    @Pattern(regexp = "^[0-9]+$", 
             message = "Goal time must be a number")
    private String goalTime;
}
