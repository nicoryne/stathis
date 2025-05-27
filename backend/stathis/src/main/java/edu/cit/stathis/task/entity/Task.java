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
@Table(name = "task")
public class Task {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "task_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "physical_id")
    private String physicalId;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
  
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "submission_date")
    private OffsetDateTime submissionDate;

    @Column(name = "closing_date")
    private OffsetDateTime closingDate;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "is_started")
    private boolean isStarted;

    @Column(name = "classroom_id")
    private String classroomPhysicalId;
    
    @Column(name = "exercise_template_id", nullable = true)
    private String exerciseTemplateId;

    @Column(name = "lesson_template_id", nullable = true)
    private String lessonTemplateId;

    @Column(name = "quiz_template_id", nullable = true)
    private String quizTemplateId;
}
