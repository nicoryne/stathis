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
@Table(name = "leaderboards")
public class Leaderboard {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "leaderboard_id", updatable = false, nullable = false)
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

    @Column(name = "score")
    private int score;

    @Column(name = "time_taken")
    private long timeTaken; // in milliseconds

    @Column(name = "rank")
    private int rank;

    @Column(name = "accuracy")
    private double accuracy;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
} 