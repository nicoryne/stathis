package edu.cit.stathis.task.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "task_completions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompletion {
    @Id
    private String physicalId;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String taskId;

    @Column(nullable = false)
    private boolean lessonCompleted;

    @Column(nullable = false)
    private boolean quizCompleted;

    @Column(nullable = false)
    private boolean exerciseCompleted;

    @Column(nullable = false)
    private boolean isFullyCompleted;

    @Column
    private Long totalTimeTaken;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    @Column
    private OffsetDateTime completedAt;
} 