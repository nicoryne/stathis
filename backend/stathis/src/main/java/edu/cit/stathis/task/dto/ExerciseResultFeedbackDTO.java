package edu.cit.stathis.task.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseResultFeedbackDTO {
    private boolean valid;
    private String message;
    private boolean goalMet;
} 