package edu.cit.stathis.vitals.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VitalSignsDTO {
    private String physicalId;
    private String studentId;
    private String classroomId;
    private String taskId;
    private Integer heartRate;
    private Integer oxygenSaturation;
    private LocalDateTime timestamp;
    private Boolean isPreActivity;
    private Boolean isPostActivity;
} 