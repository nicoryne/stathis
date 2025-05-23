package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import edu.cit.stathis.task.service.AchievementService;
import edu.cit.stathis.task.repository.BadgeRepository;
import edu.cit.stathis.task.repository.LeaderboardRepository;
import edu.cit.stathis.task.entity.Badge;
import edu.cit.stathis.task.entity.Leaderboard;
import edu.cit.stathis.task.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@Tag(name = "Achievements", description = "Endpoints related to student achievements and task completion")
@CrossOrigin
public class AchievementController {
    @Autowired
    private AchievementService achievementService;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Operation(summary = "Get badges", description = "Retrieve badges for a student or task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved badges"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponseDTO>> getBadges(
            @Parameter(description = "Student ID to filter badges") @RequestParam(required = false) String studentId,
            @Parameter(description = "Task ID to filter badges") @RequestParam(required = false) String taskId) {
        try {
            List<Badge> badges;
            if (studentId != null) {
                badges = badgeRepository.findByStudentId(studentId);
            } else if (taskId != null) {
                badges = badgeRepository.findByTaskId(taskId);
            } else {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(badges.stream()
                .map(this::buildBadgeResponse)
                .toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get leaderboard", description = "Retrieve leaderboard entries for a task or student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved leaderboard"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponseDTO>> getLeaderboard(
            @Parameter(description = "Task ID to filter leaderboard") @RequestParam(required = false) String taskId,
            @Parameter(description = "Student ID to filter leaderboard") @RequestParam(required = false) String studentId,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "score") String sortBy,
            @Parameter(description = "Sort order (asc/desc)") @RequestParam(defaultValue = "desc") String order) {
        try {
            List<Leaderboard> entries;
            if (taskId != null) {
                entries = leaderboardRepository.findByTaskIdOrderByScoreDescTimeTakenAsc(taskId);
            } else if (studentId != null) {
                entries = leaderboardRepository.findByStudentId(studentId);
            } else {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(entries.stream()
                .map(this::buildLeaderboardResponse)
                .toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Complete task", description = "Mark a task as completed for a student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or task already completed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/task-completion")
    public ResponseEntity<TaskCompletionResponseDTO> completeTask(
            @Valid @RequestBody TaskCompletionRequestDTO request) {
        try {
            achievementService.completeTask(request.getStudentId(), request.getTaskId());
            return new ResponseEntity<>(buildTaskCompletionResponse(request), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                TaskCompletionResponseDTO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private BadgeResponseDTO buildBadgeResponse(Badge badge) {
        return BadgeResponseDTO.builder()
            .physicalId(badge.getPhysicalId())
            .studentId(badge.getStudentId())
            .taskId(badge.getTaskId())
            .badgeType(badge.getBadgeType())
            .description(badge.getDescription())
            .earnedAt(badge.getEarnedAt())
            .build();
    }

    private LeaderboardResponseDTO buildLeaderboardResponse(Leaderboard entry) {
        return LeaderboardResponseDTO.builder()
            .physicalId(entry.getPhysicalId())
            .studentId(entry.getStudentId())
            .taskId(entry.getTaskId())
            .score(entry.getScore())
            .timeTaken(entry.getTimeTaken())
            .accuracy(entry.getAccuracy())
            .rank(entry.getRank())
            .completedAt(entry.getCompletedAt())
            .build();
    }

    private TaskCompletionResponseDTO buildTaskCompletionResponse(TaskCompletionRequestDTO request) {
        return TaskCompletionResponseDTO.builder()
            .success(true)
            .message("Task completed successfully")
            .studentId(request.getStudentId())
            .taskId(request.getTaskId())
            .build();
    }
}

class TaskCompletionRequest {
    private String studentId;
    private String taskId;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
} 