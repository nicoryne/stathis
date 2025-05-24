package edu.cit.stathis.vitals.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HeartRateAlertDTO {
    private String studentId;
    private String studentName;
    private Integer currentHeartRate;
    private Integer thresholdHeartRate;
    private String alertMessage;
    private LocalDateTime timestamp;
} 