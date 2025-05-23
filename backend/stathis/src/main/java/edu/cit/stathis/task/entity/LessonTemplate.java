package edu.cit.stathis.task.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Map;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "lesson_template")
public class LessonTemplate {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "lesson_template_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "physical_id")
    private String physicalId;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Type(JsonType.class)
    @Column(name = "content", columnDefinition = "jsonb")
    private Map<String, Object> content;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;
}
