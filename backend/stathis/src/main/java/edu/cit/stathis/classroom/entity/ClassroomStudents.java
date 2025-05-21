package edu.cit.stathis.classroom.entity;

import jakarta.persistence.*;
import lombok.*;
import edu.cit.stathis.auth.entity.User;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "classroom_students")
public class ClassroomStudents {
    
    @Id
    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @Id
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "is_verified")
    private boolean isVerified;
}
