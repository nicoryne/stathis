package edu.cit.stathis.task.dto;

import java.util.Map;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonTemplateResponseDTO {
    private String physicalId;
    private String title;
    private String description;
    private Map<String, Object> content;
}
