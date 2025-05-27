package edu.cit.stathis.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_profile")
public class UserProfile {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Version
  @Column(name = "version")
  private Long version;

  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "birthdate")
  private LocalDate birthdate;

  @Column(name = "profile_picture_url")
  private String profilePictureUrl;

  @Column(name = "school")
  private String school;

  @Column(name = "course")
  private String course;

  @Column(name = "year_level")
  private Integer yearLevel;

  @Column(name = "department")
  private String department;

  @Column(name = "position_title")
  private String positionTitle;

  @Column(name = "height_in_meters")
  private Double heightInMeters;

  @Column(name = "weight_in_kg")
  private Double weightInKg;

  @CreationTimestamp
  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Transient
  public Integer getAge() {
    if (birthdate == null) return null;

    LocalDate dateNow = LocalDate.now();

    return dateNow.getYear()
        - birthdate.getYear()
        - (dateNow.getDayOfYear() < birthdate.getDayOfYear() ? 1 : 0);
  }

  @Transient
  public Double getBmi() {
    if (heightInMeters == null || weightInKg == null || heightInMeters == 0) return null;

    return weightInKg / (heightInMeters * heightInMeters);
  }

  public String getBmiFormatted() {
    Double bmi = getBmi();
    return bmi != null ? String.format("%.2f", bmi) : null;
  }
}
