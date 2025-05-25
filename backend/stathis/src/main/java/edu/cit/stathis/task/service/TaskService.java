package edu.cit.stathis.task.service;

import edu.cit.stathis.task.dto.TaskBodyDTO;
import edu.cit.stathis.task.entity.Task;
import edu.cit.stathis.task.repository.TaskRepository;
import edu.cit.stathis.classroom.service.ClassroomService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ClassroomService classroomService;

    @Transactional
    @PreAuthorize("hasRole('TEACHER')")
    public Task createTask(TaskBodyDTO taskBodyDTO) {
        validateTaskBody(taskBodyDTO);
        
        Task task = Task.builder()
                .physicalId(generatePhysicalId())
                .name(taskBodyDTO.getName())
                .description(taskBodyDTO.getDescription())
                .submissionDate(OffsetDateTime.parse(taskBodyDTO.getSubmissionDate()))
                .closingDate(OffsetDateTime.parse(taskBodyDTO.getClosingDate()))
                .imageUrl(taskBodyDTO.getImageUrl())
                .classroomPhysicalId(taskBodyDTO.getClassroomPhysicalId())
                .exerciseTemplateId(taskBodyDTO.getExerciseTemplateId())
                .lessonTemplateId(taskBodyDTO.getLessonTemplateId())
                .quizTemplateId(taskBodyDTO.getQuizTemplateId())
                .maxAttempts(taskBodyDTO.getMaxAttempts() != null ? taskBodyDTO.getMaxAttempts() : 1)
                .isActive(true)
                .isStarted(false)
                .build();

        return taskRepository.save(task);
    }

    @Transactional
    @PreAuthorize("hasRole('TEACHER')")
    public Task updateTask(String physicalId, TaskBodyDTO taskBodyDTO) {
        validateTaskBody(taskBodyDTO);
        
        Task task = taskRepository.findByPhysicalId(physicalId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with physical ID: " + physicalId));

        task.setName(taskBodyDTO.getName());
        task.setDescription(taskBodyDTO.getDescription());
        task.setSubmissionDate(OffsetDateTime.parse(taskBodyDTO.getSubmissionDate()));
        task.setClosingDate(OffsetDateTime.parse(taskBodyDTO.getClosingDate()));
        task.setImageUrl(taskBodyDTO.getImageUrl());
        task.setExerciseTemplateId(taskBodyDTO.getExerciseTemplateId());
        task.setLessonTemplateId(taskBodyDTO.getLessonTemplateId());
        task.setQuizTemplateId(taskBodyDTO.getQuizTemplateId());
        if (taskBodyDTO.getMaxAttempts() != null) {
            task.setMaxAttempts(taskBodyDTO.getMaxAttempts());
        }

        return taskRepository.save(task);
    }

    @Transactional
    @PreAuthorize("hasRole('TEACHER')")
    public void deleteTask(String physicalId) {
        Task task = taskRepository.findByPhysicalId(physicalId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with physical ID: " + physicalId));
        taskRepository.delete(task);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public Optional<Task> getTaskByPhysicalId(String physicalId) {
        return taskRepository.findByPhysicalId(physicalId);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public List<Task> getTasksByClassroom(String classroomPhysicalId) {
        return taskRepository.findByClassroomPhysicalId(classroomPhysicalId);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public List<Task> getActiveTasksByClassroom(String classroomPhysicalId) {
        return taskRepository.findActiveTasksByClassroom(classroomPhysicalId);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public List<Task> getStartedTasksByClassroom(String classroomPhysicalId) {
        return taskRepository.findStartedTasksByClassroom(classroomPhysicalId);
    }

    @Transactional
    @PreAuthorize("hasRole('TEACHER')")
    public void startTask(String physicalId) {
        Task task = taskRepository.findByPhysicalId(physicalId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with physical ID: " + physicalId));
        
        if (task.isStarted()) {
            throw new IllegalStateException("Task is already started");
        }
        if (!task.isActive()) {
            throw new IllegalStateException("Cannot start an inactive task");
        }
        
        task.setStarted(true);
        taskRepository.save(task);
    }

    @Transactional
    @PreAuthorize("hasRole('TEACHER')")
    public void deactivateTask(String physicalId) {
        Task task = taskRepository.findByPhysicalId(physicalId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with physical ID: " + physicalId));
        task.setActive(false);
        taskRepository.save(task);
    }

    public boolean existsByPhysicalId(String physicalId) {
        return taskRepository.existsByPhysicalId(physicalId);
    }

    public boolean existsByPhysicalIdAndClassroomId(String physicalId, String classroomId) {
        return taskRepository.existsByPhysicalIdAndClassroomId(physicalId, classroomId);
    }

    private String generatePhysicalId() {
        return "TASK-" + UUID.randomUUID().toString().toUpperCase();
    }

    private void validateTaskBody(TaskBodyDTO taskBodyDTO) {
        if (!StringUtils.hasText(taskBodyDTO.getName())) {
            throw new IllegalArgumentException("Task name is required");
        }
        if (!StringUtils.hasText(taskBodyDTO.getClassroomPhysicalId())) {
            throw new IllegalArgumentException("Classroom ID is required");
        }
        
        OffsetDateTime submissionDate = OffsetDateTime.parse(taskBodyDTO.getSubmissionDate());
        OffsetDateTime closingDate = OffsetDateTime.parse(taskBodyDTO.getClosingDate());
        
        if (submissionDate.isAfter(closingDate)) {
            throw new IllegalArgumentException("Submission date must be before closing date");
        }
        
        try {
            classroomService.getClassroomById(taskBodyDTO.getClassroomPhysicalId());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Classroom not found");
        }
    }
}
