package edu.cit.stathis.auth.dto;

import edu.cit.stathis.auth.enums.UserRoleEnum;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDTO {

  @NotBlank(message = "Email is required.")
  @Email(message = "Email should be valid.")
  private String email;

  @NotBlank(message = "Password is required")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
      message =
          "Password must be at least 8 characters long and include an uppercase letter, lowercase"
              + " letter, number, and special character")
  private String password;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @NotBlank(message = "Role is required")
  private UserRoleEnum userRole;
}
