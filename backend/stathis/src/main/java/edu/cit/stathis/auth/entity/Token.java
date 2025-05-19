package edu.cit.stathis.auth.entity;

import edu.cit.stathis.auth.enums.TokenTypeEnum;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tokens")
public class Token {

  @Id
  @GeneratedValue
  @Column(name = "token_id")
  private UUID tokenId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "token_type", nullable = false)
  private TokenTypeEnum tokenType;

  @Column(name = "token_hash", nullable = false)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "revoked")
  @Builder.Default
  private boolean revoked = false;

  @Column(name = "used_at")
  private OffsetDateTime usedAt;

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "device_info")
  private String deviceInfo;

  @Column(name = "context")
  private String context;

  @Column(name = "created_at")
  @CreationTimestamp
  private OffsetDateTime createdAt;
}
