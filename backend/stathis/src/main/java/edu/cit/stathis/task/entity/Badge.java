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
@Table(name = "badges")
public class Badge {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "badge_id", updatable = false, nullable = false)
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

    @Column(name = "badge_type")
    private String badgeType;

    @Column(name = "description")
    private String description;

    @Column(name = "earned_at")
    private OffsetDateTime earnedAt;
} 