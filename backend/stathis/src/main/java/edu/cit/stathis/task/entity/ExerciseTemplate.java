package edu.cit.stathis.task.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import edu.cit.stathis.task.enums.ExerciseType;
import edu.cit.stathis.task.enums.ExerciseDifficulty;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "exercise_template")
public class ExerciseTemplate {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "exercise_template_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "physical_id")
    private String physicalId;

    @Column(name = "teacher_physical_id")
    private String teacherPhysicalId;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "exercise_type")
    private ExerciseType exerciseType;

    @Column(name = "exercise_difficulty")
    private ExerciseDifficulty exerciseDifficulty;

    @Column(name = "goal_reps")
    private int goalReps;

    @Column(name = "goal_accuracy")
    private int goalAccuracy;

    @Column(name = "goal_time")
    private int goalTime;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;
    
}
