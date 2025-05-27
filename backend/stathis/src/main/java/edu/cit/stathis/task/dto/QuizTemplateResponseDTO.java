package edu.cit.stathis.task.dto;

import java.util.Map;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizTemplateResponseDTO {
    private String physicalId;
    private String title;
    private String instruction;
    private int maxScore;
    private Map<String, Object> content;
}
