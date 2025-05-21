package edu.cit.stathis.classroom.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "classroom")
public class Classroom {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "classroom_id", updatable = false, nullable = false)
    private UUID id;

    @Column(length = 11, name = "physical_id", nullable = false, unique = true)
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
    
    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "teacher_id")
    private String teacherId;
    
    @JsonIgnoreProperties("classroom")
    @Column(name = "classroom_students")
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassroomStudents> classroomStudents;
    
}
