package edu.cit.stathis.task.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import edu.cit.stathis.task.repository.TaskRepository;
import edu.cit.stathis.task.entity.Task;
import edu.cit.stathis.task.dto.TaskBodyDTO;
import edu.cit.stathis.task.dto.TaskResponseDTO;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TaskService {
    @Autowired 
    private TaskRepository taskRepository;

    public Task createTask(TaskBodyDTO taskBodyDTO) {
        Task task = new Task();
        task.setName(taskBodyDTO.getName());
        task.setDescription(taskBodyDTO.getDescription());
        task.setSubmissionDate(OffsetDateTime.parse(taskBodyDTO.getSubmissionDate()));
        task.setClosingDate(OffsetDateTime.parse(taskBodyDTO.getClosingDate()));
        task.setImageUrl(taskBodyDTO.getImageUrl());
        task.setClassroomPhysicalId(taskBodyDTO.getClassroomPhysicalId());
        task.setExerciseTemplateId(taskBodyDTO.getExerciseTemplateId());
        task.setLessonTemplateId(taskBodyDTO.getLessonTemplateId());
        task.setQuizTemplateId(taskBodyDTO.getQuizTemplateId());
        task.setActive(true);
        return taskRepository.save(task);
    }

    public List<Task> getTasksByClassroomPhysicalId(String classroomPhysicalId) {
        return taskRepository.findByClassroomPhysicalId(classroomPhysicalId);
    }

    public Task getTaskByPhysicalId(String physicalId) {
        return taskRepository.findByPhysicalId(physicalId);
    }

    public Task updateTaskByPhysicalId(String physicalId, TaskBodyDTO taskBodyDTO) {
        Task task = getTaskByPhysicalId(physicalId);
        task.setName(taskBodyDTO.getName());
        task.setDescription(taskBodyDTO.getDescription());
        task.setSubmissionDate(OffsetDateTime.parse(taskBodyDTO.getSubmissionDate()));
        task.setClosingDate(OffsetDateTime.parse(taskBodyDTO.getClosingDate()));
        task.setImageUrl(taskBodyDTO.getImageUrl());
        task.setClassroomPhysicalId(taskBodyDTO.getClassroomPhysicalId());
        task.setExerciseTemplateId(taskBodyDTO.getExerciseTemplateId());
        task.setLessonTemplateId(taskBodyDTO.getLessonTemplateId());
        task.setQuizTemplateId(taskBodyDTO.getQuizTemplateId());
        return taskRepository.save(task);
    }

    public void deleteTaskByPhysicalId(String physicalId) {
        Task task = getTaskByPhysicalId(physicalId);
        taskRepository.deleteById(task.getId());
    }

    public TaskResponseDTO getTaskResponseDTO(String physicalId) {
        Task task = getTaskByPhysicalId(physicalId);
        return TaskResponseDTO.builder()
            .physicalId(task.getPhysicalId())
            .name(task.getName())
            .description(task.getDescription())
            .submissionDate(task.getSubmissionDate().toString())
            .closingDate(task.getClosingDate().toString())
            .imageUrl(task.getImageUrl())
            .classroomPhysicalId(task.getClassroomPhysicalId())
            .exerciseTemplateId(task.getExerciseTemplateId())
            .lessonTemplateId(task.getLessonTemplateId())
            .quizTemplateId(task.getQuizTemplateId())
            .isActive(task.isActive())
            .createdAt(task.getCreatedAt().toString())
            .updatedAt(task.getUpdatedAt().toString())
            .build();
    }
}
