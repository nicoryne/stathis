package edu.cit.stathis.task.dto;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@Builder
public class LeaderboardResponseDTO {
    @NotBlank
    private String physicalId;
    
    @NotBlank
    private String studentId;
    
    @NotBlank
    private String taskId;
    
    @Min(0)
    private int score;
    
    @Min(0)
    private long timeTaken;
    
    @Min(0)
    private double accuracy;
    
    @Min(1)
    private int rank;
    
    @NotNull
    private OffsetDateTime completedAt;
} 