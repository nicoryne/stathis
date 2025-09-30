package edu.cit.stathis.posture.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandmarkRequest {
  public float[][] landmarks;
}
