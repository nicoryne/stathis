package edu.cit.stathis.task.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import edu.cit.stathis.task.repository.BadgeRepository;
import edu.cit.stathis.task.repository.LeaderboardRepository;
import edu.cit.stathis.task.repository.ScoreRepository;
import edu.cit.stathis.task.repository.TaskCompletionRepository;
import edu.cit.stathis.task.entity.Badge;
import edu.cit.stathis.task.entity.Leaderboard;
import edu.cit.stathis.task.entity.Score;
import edu.cit.stathis.task.entity.TaskCompletion;
import edu.cit.stathis.task.enums.BadgeType;
import edu.cit.stathis.task.enums.ExerciseDifficulty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AchievementService {
    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private TaskCompletionRepository taskCompletionRepository;

    @Transactional
    public void startTask(String studentId, String taskId) {
        TaskCompletion taskCompletion = taskCompletionRepository.findByStudentIdAndTaskId(studentId, taskId);
        if (taskCompletion == null) {
            taskCompletion = TaskCompletion.builder()
                .physicalId(generatePhysicalId())
                .studentId(studentId)
                .taskId(taskId)
                .startedAt(OffsetDateTime.now())
                .build();
            taskCompletionRepository.save(taskCompletion);
        }
    }

    @Transactional
    public void updateTaskProgress(String studentId, String taskId, String componentType, boolean completed) {
        TaskCompletion taskCompletion = taskCompletionRepository.findByStudentIdAndTaskId(studentId, taskId);
        if (taskCompletion == null) {
            taskCompletion = TaskCompletion.builder()
                .physicalId(generatePhysicalId())
                .studentId(studentId)
                .taskId(taskId)
                .startedAt(OffsetDateTime.now())
                .build();
        }

        switch (componentType.toLowerCase()) {
            case "lesson":
                taskCompletion.setLessonCompleted(completed);
                break;
            case "quiz":
                taskCompletion.setQuizCompleted(completed);
                break;
            case "exercise":
                taskCompletion.setExerciseCompleted(completed);
                break;
        }

        taskCompletionRepository.save(taskCompletion);
    }

    @Transactional
    public void completeTask(String studentId, String taskId) {
        TaskCompletion taskCompletion = taskCompletionRepository.findByStudentIdAndTaskId(studentId, taskId);
        if (taskCompletion == null || !taskCompletion.isLessonCompleted() || 
            !taskCompletion.isQuizCompleted() || !taskCompletion.isExerciseCompleted()) {
            throw new IllegalStateException("Cannot complete task: not all components are completed");
        }

        taskCompletion.setFullyCompleted(true);
        taskCompletion.setCompletedAt(OffsetDateTime.now());
        taskCompletion.setTotalTimeTaken(
            taskCompletion.getCompletedAt().toEpochSecond() - 
            taskCompletion.getStartedAt().toEpochSecond()
        );

        taskCompletionRepository.save(taskCompletion);

        // Get the best score from all components
        Score bestScore = getBestScore(studentId, taskId);
        if (bestScore != null) {
            processTaskCompletion(studentId, taskId, bestScore);
        }
    }

    private Score getBestScore(String studentId, String taskId) {
        List<Score> scores = scoreRepository.findByStudentIdAndTaskId(studentId, taskId);
        if (scores.isEmpty()) return null;

        return scores.stream()
            .max((s1, s2) -> {
                if (s1.getScore() != s2.getScore()) {
                    return Integer.compare(s1.getScore(), s2.getScore());
                }
                return Long.compare(s2.getTimeTaken(), s1.getTimeTaken());
            })
            .orElse(null);
    }

    @Transactional
    public void processTaskCompletion(String studentId, String taskId, Score score) {
        // Create task completion badge
        createBadgeIfNotExists(studentId, taskId, BadgeType.TASK_COMPLETION);

        // Check for perfect score
        if (score.getScore() == score.getMaxScore()) {
            createBadgeIfNotExists(studentId, taskId, BadgeType.PERFECT_SCORE);
        }

        // Check for speed demon (top 10% completion time)
        if (isSpeedDemon(taskId, score.getTimeTaken())) {
            createBadgeIfNotExists(studentId, taskId, BadgeType.SPEED_DEMON);
        }

        // Update leaderboard
        updateLeaderboard(studentId, taskId, score);
    }

    @Transactional
    public void processExerciseCompletion(String studentId, String taskId, String exerciseTemplateId, 
                                        double accuracy, long timeTaken, ExerciseDifficulty difficulty) {
        Score score = new Score();
        score.setStudentId(studentId);
        score.setTaskId(taskId);
        score.setExerciseTemplateId(exerciseTemplateId);
        score.setTimeTaken(timeTaken);
        score.setAccuracy(accuracy);
        score.setCompleted(true);
        score.setCompletedAt(OffsetDateTime.now());

        // Calculate score based on difficulty
        if (difficulty == ExerciseDifficulty.BEGINNER) {
            score.setScore(accuracy == 100.0 ? 100 : 0);
            score.setMaxScore(100);
        } else { // EXPERT
            // Get exercise template to check goal accuracy
            // If accuracy is below goal, apply deduction
            // Implementation depends on your exercise template structure
        }

        scoreRepository.save(score);
        updateTaskProgress(studentId, taskId, "exercise", true);
    }

    private void createBadgeIfNotExists(String studentId, String taskId, BadgeType badgeType) {
        if (!badgeRepository.existsByStudentIdAndTaskIdAndBadgeType(studentId, taskId, badgeType.name())) {
            Badge badge = Badge.builder()
                .physicalId(generatePhysicalId())
                .studentId(studentId)
                .taskId(taskId)
                .badgeType(badgeType.name())
                .description(badgeType.getDescription())
                .earnedAt(OffsetDateTime.now())
                .build();
            badgeRepository.save(badge);
        }
    }

    private void updateLeaderboard(String studentId, String taskId, Score score) {
        Leaderboard leaderboard = leaderboardRepository.findByStudentIdAndTaskId(studentId, taskId);
        if (leaderboard == null) {
            leaderboard = new Leaderboard();
            leaderboard.setPhysicalId(generatePhysicalId());
            leaderboard.setStudentId(studentId);
            leaderboard.setTaskId(taskId);
        }

        leaderboard.setScore(score.getScore());
        leaderboard.setTimeTaken(score.getTimeTaken());
        leaderboard.setAccuracy(score.getAccuracy());
        leaderboard.setCompletedAt(score.getCompletedAt());

        leaderboardRepository.save(leaderboard);

        // Update ranks for all entries
        updateRanks(taskId);
    }

    private void updateRanks(String taskId) {
        List<Leaderboard> entries = leaderboardRepository.findByTaskIdOrderByScoreDescTimeTakenAsc(taskId);
        for (int i = 0; i < entries.size(); i++) {
            Leaderboard entry = entries.get(i);
            entry.setRank(i + 1);
            leaderboardRepository.save(entry);
        }
    }

    private boolean isSpeedDemon(String taskId, long timeTaken) {
        List<Leaderboard> entries = leaderboardRepository.findByTaskIdOrderByScoreDescTimeTakenAsc(taskId);
        if (entries.size() < 10) return false;

        int top10Percent = Math.max(1, entries.size() / 10);
        return entries.stream()
            .limit(top10Percent)
            .anyMatch(entry -> entry.getTimeTaken() >= timeTaken);
    }

    private String generatePhysicalId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%04d", random.nextInt(10000));
        String thirdPart = String.format("%03d", random.nextInt(1000));
        return String.format("ACH-%s-%s-%s", year, secondPart, thirdPart);
    }
} 