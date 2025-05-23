package edu.cit.stathis.task.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class QuizTemplateDTO {
    private String physicalId;
    private String title;
    private String instruction;
    private int maxScore;
    private Map<String, Object> content;
} 