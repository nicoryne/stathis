package edu.cit.stathis.auth.entity;

import edu.cit.stathis.auth.enums.UserRoleEnum;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(generator = "UUID")
  @Column(name = "user_id", updatable = false, nullable = false)
  private UUID userId;

  @Version
  @Column(name = "version")
  private Long version;

  @Column(length = 11, name = "physical_id", nullable = false, unique = true)
  private String physicalId;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_role", nullable = false)
  @Builder.Default
  private UserRoleEnum userRole = UserRoleEnum.GUEST_USER;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "email_verified")
  @Builder.Default
  private boolean emailVerified = false;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
