package edu.cit.stathis.auth.dto;

import edu.cit.stathis.auth.enums.UserRoleEnum;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

  private String physicalId;
  private String email;
  private String firstName;
  private String lastName;
  private LocalDate birthdate;
  private String profilePictureUrl;
  private UserRoleEnum role;
  private String school;
  private String course;
  private Integer yearLevel;
  private String department;
  private String positionTitle;
}
