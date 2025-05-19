package edu.cit.stathis.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTeacherProfileDTO {

  @NotBlank(message = "School is required")
  private String school;

  private String department;

  private String positionTitle;
}
