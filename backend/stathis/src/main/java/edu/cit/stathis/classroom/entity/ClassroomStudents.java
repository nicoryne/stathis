package edu.cit.stathis.classroom.entity;

import jakarta.persistence.*;
import lombok.*;
import edu.cit.stathis.auth.entity.UserProfile;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "classroom_students")
public class ClassroomStudents {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "classroom_students_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserProfile student;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "is_verified")
    private boolean isVerified;
}
