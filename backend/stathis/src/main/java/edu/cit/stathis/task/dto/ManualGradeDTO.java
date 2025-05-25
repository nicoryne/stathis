package edu.cit.stathis.task.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualGradeDTO {
    private String studentId;
    private String taskId;
    private Integer manualScore;
    private String teacherFeedback;
} 