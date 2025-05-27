package edu.cit.stathis.task.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import edu.cit.stathis.task.repository.TaskRepository;
import edu.cit.stathis.task.repository.ScoreRepository;
import edu.cit.stathis.task.entity.Task;
import edu.cit.stathis.task.entity.Score;
import edu.cit.stathis.task.dto.StudentTaskResponseDTO;
import edu.cit.stathis.task.dto.LessonTemplateResponseDTO;
import edu.cit.stathis.task.dto.QuizTemplateResponseDTO;
import edu.cit.stathis.task.dto.ExerciseTemplateResponseDTO;
import edu.cit.stathis.task.dto.ScoreDTO;
import edu.cit.stathis.task.repository.LessonTemplateRepository;
import edu.cit.stathis.task.repository.QuizTemplateRepository;
import edu.cit.stathis.task.repository.ExerciseTemplateRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class StudentTaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private LessonTemplateRepository lessonTemplateRepository;

    @Autowired
    private QuizTemplateRepository quizTemplateRepository;

    @Autowired
    private ExerciseTemplateRepository exerciseTemplateRepository;

    public List<StudentTaskResponseDTO> getStudentTasks(String studentId) {
        List<Task> tasks = taskRepository.findByClassroomPhysicalId(studentId);
        return tasks.stream()
            .map(task -> buildStudentTaskResponse(task, studentId))
            .collect(Collectors.toList());
    }

    public StudentTaskResponseDTO getStudentTask(String taskId, String studentId) {
        Task task = taskRepository.findByPhysicalId(taskId);
        return buildStudentTaskResponse(task, studentId);
    }

    public Score submitQuizScore(String studentId, String taskId, String quizTemplateId, int score) {
        Score existingScore = scoreRepository.findByStudentIdAndTaskIdAndQuizTemplateId(
            studentId, taskId, quizTemplateId);

        if (existingScore == null) {
            existingScore = new Score();
            existingScore.setPhysicalId(provideUniquePhysicalId());
            existingScore.setStudentId(studentId);
            existingScore.setTaskId(taskId);
            existingScore.setQuizTemplateId(quizTemplateId);
            existingScore.setAttempts(0);
        }

        existingScore.setScore(score);
        existingScore.setAttempts(existingScore.getAttempts() + 1);
        existingScore.setCompleted(true);

        return scoreRepository.save(existingScore);
    }

    private StudentTaskResponseDTO buildStudentTaskResponse(Task task, String studentId) {
        Score score = null;
        if (task.getQuizTemplateId() != null) {
            score = scoreRepository.findByStudentIdAndTaskIdAndQuizTemplateId(
                studentId, task.getPhysicalId(), task.getQuizTemplateId());
        }

        return StudentTaskResponseDTO.builder()
            .physicalId(task.getPhysicalId())
            .name(task.getName())
            .description(task.getDescription())
            .submissionDate(task.getSubmissionDate().toString())
            .closingDate(task.getClosingDate().toString())
            .imageUrl(task.getImageUrl())
            .classroomPhysicalId(task.getClassroomPhysicalId())
            .lessonTemplate(task.getLessonTemplateId() != null ? 
                buildLessonTemplateDTO(task.getLessonTemplateId()) : null)
            .quizTemplate(task.getQuizTemplateId() != null ? 
                buildQuizTemplateDTO(task.getQuizTemplateId()) : null)
            .exerciseTemplate(task.getExerciseTemplateId() != null ? 
                buildExerciseTemplateDTO(task.getExerciseTemplateId()) : null)
            .score(score != null ? buildScoreDTO(score) : null)
            .isCompleted(score != null && score.isCompleted())
            .isStarted(task.isStarted())
            .createdAt(task.getCreatedAt().toString())
            .updatedAt(task.getUpdatedAt().toString())
            .build();
    }

    private LessonTemplateResponseDTO buildLessonTemplateDTO(String lessonTemplateId) {
        return lessonTemplateRepository.findByPhysicalId(lessonTemplateId)
            .map(lessonTemplate -> LessonTemplateResponseDTO.builder()
                .physicalId(lessonTemplate.getPhysicalId())
                .title(lessonTemplate.getTitle())
                .description(lessonTemplate.getDescription())
                .content(lessonTemplate.getContent())
                .build())
            .orElse(null);
    }

    private QuizTemplateResponseDTO buildQuizTemplateDTO(String quizTemplateId) {
        return quizTemplateRepository.findByPhysicalId(quizTemplateId)
            .map(quizTemplate -> QuizTemplateResponseDTO.builder()
                .physicalId(quizTemplate.getPhysicalId())
                .title(quizTemplate.getTitle())
                .instruction(quizTemplate.getInstruction())
                .maxScore(quizTemplate.getMaxScore())
                .content(quizTemplate.getContent())
                .build())
            .orElse(null);
    }

    private ExerciseTemplateResponseDTO buildExerciseTemplateDTO(String exerciseTemplateId) {
        return exerciseTemplateRepository.findByPhysicalId(exerciseTemplateId)
            .map(exerciseTemplate -> ExerciseTemplateResponseDTO.builder()
                .physicalId(exerciseTemplate.getPhysicalId())
                .title(exerciseTemplate.getTitle())
                .description(exerciseTemplate.getDescription())
                .exerciseType(exerciseTemplate.getExerciseType())
                .exerciseDifficulty(exerciseTemplate.getExerciseDifficulty())
                .goalReps(exerciseTemplate.getGoalReps())
                .goalAccuracy(exerciseTemplate.getGoalAccuracy())
                .goalTime(exerciseTemplate.getGoalTime())
                .build())
            .orElse(null);
    }

    private ScoreDTO buildScoreDTO(Score score) {
        return ScoreDTO.builder()
            .physicalId(score.getPhysicalId())
            .score(score.getScore())
            .maxScore(score.getMaxScore())
            .attempts(score.getAttempts())
            .isCompleted(score.isCompleted())
            .build();
    }

    private String generatePhysicalId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%04d", random.nextInt(10000));
        String thirdPart = String.format("%03d", random.nextInt(1000));
        return String.format("SCORE-%s-%s-%s", year, secondPart, thirdPart);
    }
    
    private String provideUniquePhysicalId() {
        String generatedPhysicalId;
        do {
            generatedPhysicalId = generatePhysicalId();
        } while (scoreRepository.existsByPhysicalId(generatedPhysicalId));
        return generatedPhysicalId;
    }
} 