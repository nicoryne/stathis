package edu.cit.stathis.task.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseResultSubmissionDTO {
    private int reps;
    private double accuracy;
    private long timeTaken; // in milliseconds
} 