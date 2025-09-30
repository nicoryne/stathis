package edu.cit.stathis.task.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import edu.cit.stathis.task.repository.TaskRepository;
import edu.cit.stathis.task.repository.ScoreRepository;
import edu.cit.stathis.task.repository.TaskCompletionRepository;
import edu.cit.stathis.task.entity.Task;
import edu.cit.stathis.task.entity.Score;
import edu.cit.stathis.task.entity.TaskCompletion;
import edu.cit.stathis.task.dto.StudentTaskResponseDTO;
import edu.cit.stathis.task.dto.TaskProgressDTO;
import edu.cit.stathis.task.dto.LessonTemplateResponseDTO;
import edu.cit.stathis.task.dto.QuizTemplateResponseDTO;
import edu.cit.stathis.task.dto.ExerciseTemplateResponseDTO;
import edu.cit.stathis.task.dto.ScoreDTO;
import edu.cit.stathis.task.repository.LessonTemplateRepository;
import edu.cit.stathis.task.repository.QuizTemplateRepository;
import edu.cit.stathis.task.repository.ExerciseTemplateRepository;
import edu.cit.stathis.task.dto.QuizSubmissionDTO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import edu.cit.stathis.classroom.service.ClassroomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class StudentTaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private TaskCompletionRepository taskCompletionRepository;

    @Autowired
    private LessonTemplateRepository lessonTemplateRepository;

    @Autowired
    private QuizTemplateRepository quizTemplateRepository;

    @Autowired
    private ExerciseTemplateRepository exerciseTemplateRepository;

    @Autowired
    private ClassroomService classroomService;

    @Transactional(readOnly = true)
    public List<StudentTaskResponseDTO> getStudentTasks(String classroomPhysicalId, String studentId) {
        // Only allow if student is enrolled and verified
        if (!classroomService.isUserEnrolledAndVerifiedInClassroom(studentId, classroomPhysicalId)) {
            throw new RuntimeException("You are not authorized to view tasks for this classroom (not verified)");
        }
        List<Task> tasks = taskRepository.findByClassroomPhysicalId(classroomPhysicalId);
        return tasks.stream()
            .map(task -> buildStudentTaskResponse(task, studentId))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentTaskResponseDTO getStudentTask(String taskId, String studentId) {
        Task task = taskRepository.findByPhysicalId(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        return buildStudentTaskResponse(task, studentId);
    }

    @Transactional(readOnly = true)
    public TaskProgressDTO getTaskProgress(String taskId, String studentId) {
        Task task = taskRepository.findByPhysicalId(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        TaskCompletion completion = taskCompletionRepository.findByStudentIdAndTaskId(studentId, taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task completion not found for student ID: " + studentId + " and task ID: " + taskId));
        Score score = task.getQuizTemplateId() != null ? 
            scoreRepository.findQuizScore(studentId, taskId, task.getQuizTemplateId()).orElse(null) : null;

        if (completion == null) {
            return TaskProgressDTO.builder()
                .lessonCompleted(false)
                .exerciseCompleted(false)
                .quizCompleted(false)
                .quizScore(0)
                .maxQuizScore(0)
                .quizAttempts(0)
                .totalTimeTaken(0L)
                .build();
        }

        return TaskProgressDTO.builder()
            .lessonCompleted(completion.isLessonCompleted())
            .exerciseCompleted(completion.isExerciseCompleted())
            .quizCompleted(completion.isQuizCompleted())
            .quizScore(score != null ? score.getScore() : 0)
            .maxQuizScore(score != null ? score.getMaxScore() : 0)
            .quizAttempts(score != null ? score.getAttempts() : 0)
            .totalTimeTaken(completion.getTotalTimeTaken())
            .startedAt(completion.getStartedAt().toString())
            .completedAt(completion.getCompletedAt() != null ? completion.getCompletedAt().toString() : null)
            .build();
    }

    @Transactional
    public Score submitQuizScore(String studentId, String taskId, String quizTemplateId, int score) {
        Score existingScore = scoreRepository.findQuizScore(studentId, taskId, quizTemplateId)
            .orElse(null);

        if (existingScore == null) {
            existingScore = new Score();
            existingScore.setPhysicalId(provideUniquePhysicalId());
            existingScore.setStudentId(studentId);
            existingScore.setTaskId(taskId);
            existingScore.setQuizTemplateId(quizTemplateId);
            existingScore.setAttempts(0);
        }

        // Enforce max attempts based on Task configuration
        Task task = taskRepository.findByPhysicalId(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        validateAttempts(task, existingScore);

        // Ensure maxScore is populated from quiz template if available
        if (existingScore.getMaxScore() <= 0) {
            var optTemplate = quizTemplateRepository.findByPhysicalId(quizTemplateId);
            if (optTemplate.isPresent()) {
                var quizTemplate = optTemplate.get();
                int templateMax = 0;
                try {
                    Object quizNode = quizTemplate.getContent() != null ? quizTemplate.getContent().get("quiz") : null;
                    if (quizNode instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        var quizMap = (java.util.Map<String, Object>) quizNode;
                        Object maxScoreNode = quizMap.get("maxScore");
                        if (maxScoreNode instanceof Number) {
                            templateMax = ((Number) maxScoreNode).intValue();
                        } else {
                            Object contentNode = quizMap.get("content");
                            if (contentNode instanceof java.util.Map) {
                                @SuppressWarnings("unchecked")
                                var contentMap = (java.util.Map<String, Object>) contentNode;
                                Object questionsNode = contentMap.get("questions");
                                if (questionsNode instanceof java.util.List) {
                                    templateMax = ((java.util.List<?>) questionsNode).size();
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
                if (templateMax <= 0) {
                    templateMax = quizTemplate.getMaxScore();
                }
                existingScore.setMaxScore(templateMax);
            }
        }

        existingScore.setScore(score);
        existingScore.setAttempts(existingScore.getAttempts() + 1);
        existingScore.setCompleted(true);
        existingScore.setCompletedAt(OffsetDateTime.now());

        Score savedScore = scoreRepository.save(existingScore);
        updateTaskCompletion(studentId, taskId, "quiz", true);
        return savedScore;
    }

    @Transactional
    public Score autoCheckQuiz(String studentId, String taskId, String quizTemplateId, QuizSubmissionDTO submission) {
        // Load quiz template JSON
        var quizTemplate = quizTemplateRepository.findByPhysicalId(quizTemplateId)
            .orElseThrow(() -> new EntityNotFoundException("Quiz template not found with ID: " + quizTemplateId));

        // Expecting structure per user's provided JSON
        // { "quiz": { "content": { "questions": [ { "answer": <index> }, ... ] }, "maxScore": n } }
        int computedScore = 0;
        int maxScore = 0;
        try {
            Object quizNode = quizTemplate.getContent().get("quiz");
            if (quizNode instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                var quizMap = (java.util.Map<String, Object>) quizNode;
                Object contentNode = quizMap.get("content");
                if (contentNode instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    var contentMap = (java.util.Map<String, Object>) contentNode;
                    Object questionsNode = contentMap.get("questions");
                    if (questionsNode instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        var questions = (java.util.List<Object>) questionsNode;
                        maxScore = questions.size();
                        for (int i = 0; i < questions.size(); i++) {
                            Object qNode = questions.get(i);
                            if (!(qNode instanceof java.util.Map)) continue;
                            @SuppressWarnings("unchecked")
                            var qMap = (java.util.Map<String, Object>) qNode;
                            Object answerObj = qMap.get("answer");
                            if (answerObj instanceof Number) {
                                int correctIndex = ((Number) answerObj).intValue();
                                int studentIndex = (submission.getAnswers() != null && submission.getAnswers().size() > i)
                                    ? submission.getAnswers().get(i)
                                    : -1;
                                if (studentIndex == correctIndex) {
                                    computedScore += 1; // one point per question
                                }
                            }
                        }
                    }
                }
                Object maxScoreNode = quizMap.get("maxScore");
                if (maxScoreNode instanceof Number) {
                    maxScore = ((Number) maxScoreNode).intValue();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to compute quiz score from template content", ex);
        }

        // Persist using existing flow
        Score existingScore = scoreRepository.findQuizScore(studentId, taskId, quizTemplateId).orElse(null);
        if (existingScore == null) {
            existingScore = new Score();
            existingScore.setPhysicalId(provideUniquePhysicalId());
            existingScore.setStudentId(studentId);
            existingScore.setTaskId(taskId);
            existingScore.setQuizTemplateId(quizTemplateId);
            existingScore.setAttempts(0);
        }

        // Enforce max attempts based on Task configuration
        Task task = taskRepository.findByPhysicalId(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        validateAttempts(task, existingScore);

        existingScore.setScore(computedScore);
        // If template carries maxScore, set it; else fall back to question count
        existingScore.setMaxScore(maxScore);
        existingScore.setAttempts(existingScore.getAttempts() + 1);
        existingScore.setCompleted(true);
        existingScore.setCompletedAt(OffsetDateTime.now());

        Score savedScore = scoreRepository.save(existingScore);
        updateTaskCompletion(studentId, taskId, "quiz", true);
        return savedScore;
    }

    private void validateAttempts(Task task, Score existingScore) {
        if (task.getMaxAttempts() > 0 && existingScore.getAttempts() >= task.getMaxAttempts()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum quiz attempts reached for this task");
        }
    }

    @Transactional
    public void completeLesson(String studentId, String taskId, String lessonTemplateId) {
        updateTaskCompletion(studentId, taskId, "lesson", true);
    }

    @Transactional
    public void completeExercise(String studentId, String taskId, String exerciseTemplateId) {
        updateTaskCompletion(studentId, taskId, "exercise", true);
    }

    @Transactional
    private void updateTaskCompletion(String studentId, String taskId, String componentType, boolean completed) {
        TaskCompletion completion = taskCompletionRepository.findByStudentIdAndTaskId(studentId, taskId)
            .orElse(null);
        if (completion == null) {
            completion = TaskCompletion.builder()
                .physicalId(provideUniquePhysicalId())
                .studentId(studentId)
                .taskId(taskId)
                .startedAt(OffsetDateTime.now())
                .build();
        }

        switch (componentType.toLowerCase()) {
            case "lesson":
                completion.setLessonCompleted(completed);
                break;
            case "quiz":
                completion.setQuizCompleted(completed);
                break;
            case "exercise":
                completion.setExerciseCompleted(completed);
                break;
        }

        // Get the task to check which components are required
        Task task = taskRepository.findByPhysicalId(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        boolean hasLesson = task.getLessonTemplateId() != null;
        boolean hasQuiz = task.getQuizTemplateId() != null;
        boolean hasExercise = task.getExerciseTemplateId() != null;

        // Check if all required components are completed
        boolean allRequiredCompleted = true;
        if (hasLesson && !completion.isLessonCompleted()) allRequiredCompleted = false;
        if (hasQuiz && !completion.isQuizCompleted()) allRequiredCompleted = false;
        if (hasExercise && !completion.isExerciseCompleted()) allRequiredCompleted = false;

        if (allRequiredCompleted) {
            completion.setFullyCompleted(true);
            completion.setCompletedAt(OffsetDateTime.now());
            completion.setTotalTimeTaken(
                completion.getCompletedAt().toEpochSecond() - completion.getStartedAt().toEpochSecond()
            );
        }

        taskCompletionRepository.save(completion);
    }

    private String provideUniquePhysicalId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%04d", random.nextInt(10000));
        String thirdPart = String.format("%03d", random.nextInt(1000));
        return String.format("TASK-%s-%s-%s", year, secondPart, thirdPart);
    }

    private StudentTaskResponseDTO buildStudentTaskResponse(Task task, String studentId) {
        Score score = null;
        if (task.getQuizTemplateId() != null) {
            score = scoreRepository.findQuizScore(studentId, task.getPhysicalId(), task.getQuizTemplateId())
                .orElse(null);
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
} 