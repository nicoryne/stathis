package edu.cit.stathis.task.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "score")
public class Score {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "score_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "physical_id")
    private String physicalId;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
  
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "quiz_template_id")
    private String quizTemplateId;

    @Column(name = "exercise_template_id")
    private String exerciseTemplateId;

    @Column(name = "score")
    private int score;

    @Column(name = "max_score")
    private int maxScore;

    @Column(name = "attempts")
    private int attempts;

    @Column(name = "is_completed")
    private boolean isCompleted;

    @Column(name = "time_taken")
    private long timeTaken; // in milliseconds

    @Column(name = "accuracy")
    private double accuracy;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
} 