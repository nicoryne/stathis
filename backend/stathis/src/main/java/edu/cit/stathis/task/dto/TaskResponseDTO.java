package edu.cit.stathis.task.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDTO {
    private String physicalId;
    private String name;
    private String description;
    private String submissionDate;
    private String closingDate;
    private String imageUrl;
    private String classroomPhysicalId;
    private String exerciseTemplateId;
    private String lessonTemplateId;
    private String quizTemplateId;
    private boolean isActive;
    private boolean isStarted;
    private String createdAt;
    private String updatedAt;
}
