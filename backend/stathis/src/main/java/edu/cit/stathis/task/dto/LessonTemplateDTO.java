package edu.cit.stathis.task.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class LessonTemplateDTO {
    private String physicalId;
    private String title;
    private String description;
    private Map<String, Object> content;
} 