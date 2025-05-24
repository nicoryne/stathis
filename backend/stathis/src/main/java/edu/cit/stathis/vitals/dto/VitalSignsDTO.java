package edu.cit.stathis.vitals.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VitalSignsDTO {
    private Long studentId;
    private Long classroomId;
    private Long taskId;
    private Integer heartRate;
    private Integer oxygenSaturation;
    private LocalDateTime timestamp;
    private Boolean isPreActivity;
    private Boolean isPostActivity;
} 