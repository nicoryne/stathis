package edu.cit.stathis.task.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerFeedbackDTO {
    private int score;
    private int maxScore;
    private Map<String, Object> feedback; // questionId -> feedback (correct/incorrect, etc)
} 