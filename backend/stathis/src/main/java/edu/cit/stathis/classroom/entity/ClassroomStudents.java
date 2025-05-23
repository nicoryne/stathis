package edu.cit.stathis.classroom.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import edu.cit.stathis.auth.entity.UserProfile;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "classroom_students")
@Getter
@Setter
@NoArgsConstructor
public class ClassroomStudents {
    @Id
    private String physicalId;

    @ManyToOne
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserProfile student;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
