package edu.cit.stathis.task.service;

import edu.cit.stathis.task.dto.ScoreDTO;
import edu.cit.stathis.task.entity.Score;
import edu.cit.stathis.task.repository.ScoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScoreService {
    private final ScoreRepository scoreRepository;

    @Transactional
    public Score createScore(String studentId, String taskId, String templateId, boolean isQuiz) {
        Score score = Score.builder()
                .physicalId(generatePhysicalId())
                .studentId(studentId)
                .taskId(taskId)
                .quizTemplateId(isQuiz ? templateId : null)
                .exerciseTemplateId(!isQuiz ? templateId : null)
                .score(0)
                .maxScore(0)
                .attempts(0)
                .isCompleted(false)
                .timeTaken(0L)
                .accuracy(0.0)
                .startedAt(OffsetDateTime.now())
                .build();
        return scoreRepository.save(score);
    }

    @Transactional
    public Score updateScore(String physicalId, ScoreDTO scoreDTO) {
        Score score = scoreRepository.findByPhysicalId(physicalId)
                .orElseThrow(() -> new EntityNotFoundException("Score not found with physical ID: " + physicalId));
        score.setScore(scoreDTO.getScore());
        score.setMaxScore(scoreDTO.getMaxScore());
        score.setAttempts(scoreDTO.getAttempts());
        score.setCompleted(scoreDTO.isCompleted());
        score.setTeacherFeedback(scoreDTO.getTeacherFeedback());
        score.setManualScore(scoreDTO.getManualScore());
        if (scoreDTO.isCompleted()) {
            score.setCompletedAt(OffsetDateTime.now());
        }
        return scoreRepository.save(score);
    }

    @Transactional(readOnly = true)
    public Optional<Score> getScoreByPhysicalId(String physicalId) {
        return scoreRepository.findByPhysicalId(physicalId);
    }

    @Transactional(readOnly = true)
    public List<Score> getScoresByStudent(String studentId) {
        return scoreRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Score> getScoresByTask(String taskId) {
        return scoreRepository.findByTaskId(taskId);
    }

    @Transactional(readOnly = true)
    public List<Score> getScoresByStudentAndTask(String studentId, String taskId) {
        return scoreRepository.findByStudentIdAndTaskId(studentId, taskId);
    }

    @Transactional(readOnly = true)
    public Optional<Score> getQuizScore(String studentId, String taskId, String quizTemplateId) {
        return scoreRepository.findQuizScore(studentId, taskId, quizTemplateId);
    }

    public Optional<Score> getExerciseScore(String studentId, String taskId, String exerciseTemplateId) {
        return scoreRepository.findExerciseScore(studentId, taskId, exerciseTemplateId);
    }

    @Transactional(readOnly = true)
    public Double getAverageQuizScore(String taskId, String quizTemplateId) {
        return scoreRepository.getAverageQuizScore(taskId, quizTemplateId);
    }

    @Transactional(readOnly = true)
    public Double getAverageExerciseScore(String taskId, String exerciseTemplateId) {
        return scoreRepository.getAverageExerciseScore(taskId, exerciseTemplateId);
    }

    @Transactional
    public void updateManualScore(String physicalId, Integer manualScore, String teacherFeedback) {
        Score score = scoreRepository.findByPhysicalId(physicalId)
                .orElseThrow(() -> new EntityNotFoundException("Score not found with physical ID: " + physicalId));
        score.setManualScore(manualScore);
        score.setTeacherFeedback(teacherFeedback);
        scoreRepository.save(score);
    }

    @Transactional(readOnly = true)
    public boolean existsByPhysicalId(String physicalId) {
        return scoreRepository.existsByPhysicalId(physicalId);
    }

    @Transactional(readOnly = true)
    public boolean existsQuizScore(String studentId, String taskId, String quizTemplateId) {
        return scoreRepository.existsQuizScore(studentId, taskId, quizTemplateId);
    }

    @Transactional(readOnly = true)
    public boolean existsExerciseScore(String studentId, String taskId, String exerciseTemplateId) {
        return scoreRepository.existsExerciseScore(studentId, taskId, exerciseTemplateId);
    }

    private String generatePhysicalId() {
        return "SCORE-" + UUID.randomUUID().toString().toUpperCase();
    }
} 