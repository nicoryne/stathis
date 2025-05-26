package edu.cit.stathis.task.dto;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerSubmissionDTO {
    private Map<String, Object> answers;
} 