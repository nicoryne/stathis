package edu.cit.stathis.task.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseTemplateResponseDTO {
    private String physicalId;
    private String title;
    private String description;
    private String exerciseType;
    private String exerciseDifficulty;
    private int goalReps;
    private int goalAccuracy;
    private int goalTime;
}
