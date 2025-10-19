package citu.edu.stathis.mobile.features.tasks.data.repository

import citu.edu.stathis.mobile.features.tasks.data.api.TaskService
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.model.ScoreResponse
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.ExerciseTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizSubmission
import citu.edu.stathis.mobile.features.tasks.data.model.QuizAutoCheckRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskService: TaskService
) : TaskRepository {

    override suspend fun getStudentTasksForClassroom(classroomId: String): Flow<List<Task>> = flow {
        val response = taskService.getStudentTasksForClassroom(classroomId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for tasks list")
        } else {
            throw IllegalStateException("Failed to load tasks: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getStudentTask(taskId: String): Flow<Task> = flow {
        val response = taskService.getStudentTask(taskId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for task")
        } else {
            throw IllegalStateException("Failed to load task: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getTaskProgress(taskId: String): Flow<TaskProgressResponse> = flow {
        val response = taskService.getTaskProgress(taskId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for task progress")
        } else {
            throw IllegalStateException("Failed to load task progress: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getLessonTemplate(lessonTemplateId: String): Flow<LessonTemplate> = flow {
        val response = taskService.getLessonTemplate(lessonTemplateId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for lesson template")
        } else {
            throw IllegalStateException("Failed to load lesson template: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getQuizTemplate(quizTemplateId: String): Flow<QuizTemplate> = flow {
        val response = taskService.getQuizTemplate(quizTemplateId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for quiz template")
        } else {
            throw IllegalStateException("Failed to load quiz template: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getExerciseTemplate(exerciseTemplateId: String): Flow<ExerciseTemplate> = flow {
        val response = taskService.getExerciseTemplate(exerciseTemplateId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for exercise template")
        } else {
            throw IllegalStateException("Failed to load exercise template: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun submitQuizScore(
        taskId: String,
        quizTemplateId: String,
        score: Int
    ): Flow<ScoreResponse> = flow {
        val response = taskService.submitQuizScore(taskId, quizTemplateId, score)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for submit score")
        } else {
            throw IllegalStateException("Failed to submit quiz score: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun autoCheckQuiz(
        taskId: String,
        quizTemplateId: String,
        request: QuizAutoCheckRequest
    ): Flow<ScoreResponse> = flow {
        android.util.Log.d("TaskRepositoryImpl", "Auto-checking quiz: taskId=$taskId, templateId=$quizTemplateId, answers=${request.answers}")
        val response = taskService.autoCheckQuiz(taskId, quizTemplateId, request)
        android.util.Log.d("TaskRepositoryImpl", "Auto-check response: code=${response.code()}, message=${response.message()}")
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for auto-check score")
        } else {
            throw IllegalStateException("Failed to auto-check quiz: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun completeLesson(taskId: String, lessonTemplateId: String) {
        val response = taskService.completeLesson(taskId, lessonTemplateId)
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to complete lesson: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun completeExercise(taskId: String, exerciseTemplateId: String) {
        val response = taskService.completeExercise(taskId, exerciseTemplateId)
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to complete exercise: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getQuizScore(
        studentId: String,
        taskId: String,
        quizTemplateId: String
    ): Flow<ScoreResponse> = flow {
        val response = taskService.getQuizScore(studentId, taskId, quizTemplateId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for quiz score")
        } else {
            throw IllegalStateException("Failed to load quiz score: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getScoresByStudentAndTask(
        studentId: String,
        taskId: String
    ): Flow<List<ScoreResponse>> = flow {
        val response = taskService.getScoresByStudentAndTask(studentId, taskId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for scores by student and task")
        } else {
            throw IllegalStateException("Failed to load scores by student and task: ${response.code()} ${response.message()}")
        }
    }
} 