package edu.cit.stathis.task.dto;

import lombok.*;
import edu.cit.stathis.task.enums.ExerciseType;
import edu.cit.stathis.task.enums.ExerciseDifficulty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseTemplateResponseDTO {
    private String physicalId;
    private String title;
    private String description;
    private ExerciseType exerciseType;
    private ExerciseDifficulty exerciseDifficulty;
    private int goalReps;
    private int goalAccuracy;
    private int goalTime;
}
