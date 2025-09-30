package edu.cit.stathis.task.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class StudentProgressDTO {
    private String taskId;
    private String taskName;
    private String taskType; // QUIZ, LESSON, EXERCISE
    private String classroomPhysicalId;
    private Boolean completed;
    private Integer score;
    private Integer maxScore;
    private Integer attempts;
    private OffsetDateTime completedAt;
    private LocalDate submissionDate;
    private LocalDate closingDate;
}


