package edu.cit.stathis.task.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreDTO {
    private String physicalId;
    private int score;
    private int maxScore;
    private int attempts;
    private boolean isCompleted;
} 