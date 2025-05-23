package edu.cit.stathis.task.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentTaskResponseDTO {
    private String physicalId;
    private String name;
    private String description;
    private String submissionDate;
    private String closingDate;
    private String imageUrl;
    private String classroomPhysicalId;
    private LessonTemplateResponseDTO lessonTemplate;
    private QuizTemplateResponseDTO quizTemplate;
    private ExerciseTemplateResponseDTO exerciseTemplate;
    private ScoreDTO score;
    private boolean isCompleted;
    private String createdAt;
    private String updatedAt;
} 