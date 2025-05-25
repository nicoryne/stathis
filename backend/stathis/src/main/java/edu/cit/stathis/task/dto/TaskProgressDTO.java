package edu.cit.stathis.task.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskProgressDTO {
    private boolean lessonCompleted;
    private boolean exerciseCompleted;
    private boolean quizCompleted;
    private int quizScore;
    private int maxQuizScore;
    private int quizAttempts;
    private Long totalTimeTaken;
    private String startedAt;
    private String completedAt;
    private boolean submittedForReview;
    private String submittedAt;
} 