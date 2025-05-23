package edu.cit.stathis.task.dto;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
public class BadgeResponseDTO {
    @NotBlank
    private String physicalId;
    
    @NotBlank
    private String studentId;
    
    @NotBlank
    private String taskId;
    
    @NotBlank
    private String badgeType;
    
    @NotBlank
    private String description;
    
    @NotNull
    private OffsetDateTime earnedAt;
} 