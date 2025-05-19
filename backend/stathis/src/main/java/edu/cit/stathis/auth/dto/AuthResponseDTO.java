package edu.cit.stathis.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
  private String accessToken;
  private String refreshToken;
}
