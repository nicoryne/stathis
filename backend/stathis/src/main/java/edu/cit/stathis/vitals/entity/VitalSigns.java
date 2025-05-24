package edu.cit.stathis.vitals.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "vital_signs")
public class VitalSigns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "physical_id", unique = true, nullable = false)
    private String physicalId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "classroom_id", nullable = false)
    private String classroomId;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_pre_activity", nullable = false)
    private Boolean isPreActivity;

    @Column(name = "is_post_activity", nullable = false)
    private Boolean isPostActivity;

    @PrePersist
    protected void onCreate() {
        physicalId = "VITAL-" + UUID.randomUUID().toString();
    }
} 