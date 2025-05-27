package edu.cit.stathis.task.service;

import edu.cit.stathis.task.dto.TaskProgressDTO;
import edu.cit.stathis.task.entity.TaskCompletion;
import edu.cit.stathis.task.repository.TaskCompletionRepository;
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
public class TaskCompletionService {
    private final TaskCompletionRepository taskCompletionRepository;

    @Transactional
    public TaskCompletion createTaskCompletion(String studentId, String taskId) {
        TaskCompletion taskCompletion = TaskCompletion.builder()
                .physicalId(generatePhysicalId())
                .studentId(studentId)
                .taskId(taskId)
                .lessonCompleted(false)
                .quizCompleted(false)
                .exerciseCompleted(false)
                .isFullyCompleted(false)
                .startedAt(OffsetDateTime.now())
                .submittedForReview(false)
                .build();
        return taskCompletionRepository.save(taskCompletion);
    }

    @Transactional
    public TaskCompletion updateTaskProgress(String studentId, String taskId, TaskProgressDTO progressDTO) {
        TaskCompletion taskCompletion = taskCompletionRepository.findByStudentIdAndTaskId(studentId, taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task completion not found for student: " + studentId + " and task: " + taskId));
        taskCompletion.setLessonCompleted(progressDTO.isLessonCompleted());
        taskCompletion.setExerciseCompleted(progressDTO.isExerciseCompleted());
        taskCompletion.setQuizCompleted(progressDTO.isQuizCompleted());
        taskCompletion.setTotalTimeTaken(progressDTO.getTotalTimeTaken());
        if (progressDTO.getCompletedAt() != null) {
            taskCompletion.setCompletedAt(OffsetDateTime.parse(progressDTO.getCompletedAt()));
        }
        boolean isFullyCompleted = taskCompletion.isLessonCompleted() && 
                                 taskCompletion.isExerciseCompleted() && 
                                 taskCompletion.isQuizCompleted();
        taskCompletion.setFullyCompleted(isFullyCompleted);
        return taskCompletionRepository.save(taskCompletion);
    }

    public Optional<TaskCompletion> getTaskCompletion(String studentId, String taskId) {
        return taskCompletionRepository.findByStudentIdAndTaskId(studentId, taskId);
    }

    public List<TaskCompletion> getTaskCompletionsByStudent(String studentId) {
        return taskCompletionRepository.findByStudentId(studentId);
    }

    public List<TaskCompletion> getTaskCompletionsByTask(String taskId) {
        return taskCompletionRepository.findByTaskId(taskId);
    }

    public List<TaskCompletion> getCompletedTasks(String taskId) {
        return taskCompletionRepository.findCompletedByTaskId(taskId);
    }

    public List<TaskCompletion> getSubmittedForReviewTasks(String taskId) {
        return taskCompletionRepository.findSubmittedForReviewByTaskId(taskId);
    }

    public long countCompletedTasks(String taskId) {
        return taskCompletionRepository.countCompletedByTaskId(taskId);
    }

    public long countSubmittedForReviewTasks(String taskId) {
        return taskCompletionRepository.countSubmittedForReviewByTaskId(taskId);
    }

    @Transactional
    public void submitForReview(String studentId, String taskId) {
        TaskCompletion taskCompletion = taskCompletionRepository.findByStudentIdAndTaskId(studentId, taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task completion not found for student: " + studentId + " and task: " + taskId));
        taskCompletion.setSubmittedForReview(true);
        taskCompletion.setSubmittedAt(OffsetDateTime.now());
        taskCompletionRepository.save(taskCompletion);
    }

    public boolean existsByStudentIdAndTaskId(String studentId, String taskId) {
        return taskCompletionRepository.existsByStudentIdAndTaskId(studentId, taskId);
    }

    private String generatePhysicalId() {
        return "COMPLETION-" + UUID.randomUUID().toString().toUpperCase();
    }
} 