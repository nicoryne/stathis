package edu.cit.stathis.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStudentProfileDTO {

  private String school;

  private String course;

  @Min(value = 1, message = "Year level must be at least 1")
  @Max(value = 6, message = "Year level must not exceed 6")
  private Integer yearLevel;
}
