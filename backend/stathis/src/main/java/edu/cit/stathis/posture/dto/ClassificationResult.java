package edu.cit.stathis.posture.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {
  private String predictedClass;
  private float score;
  private float[] probabilities;
  private List<String> classNames;
  private Float formConfidence; // Form quality score (0.0-1.0), null for 'rest' pose
  private List<String> flags;
  private List<String> messages;
}


