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
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskService: TaskService,
    private val authTokenManager: AuthTokenManager
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
        // First try student-scoped endpoint
        val studentResp = taskService.getStudentTask(taskId)
        if (studentResp.isSuccessful) {
            val task = studentResp.body()
            if (task != null) {
                // If maxAttempts looks missing or zero, fetch authoritative task by physicalId
                if (task.maxAttempts <= 0) {
                    val fullResp = taskService.getTaskByPhysicalId(taskId)
                    if (fullResp.isSuccessful) {
                        fullResp.body()?.let { emit(it) } ?: emit(task)
                    } else {
                        emit(task)
                    }
                } else {
                    emit(task)
                }
            } else {
                throw IllegalStateException("Empty body for task")
            }
        } else {
            // Fallback to fetch by physical id if student endpoint fails
            val fallbackResp = taskService.getTaskByPhysicalId(taskId)
            if (fallbackResp.isSuccessful) {
                fallbackResp.body()?.let { emit(it) } ?: throw IllegalStateException("Empty body for task (fallback)")
            } else {
                throw IllegalStateException("Failed to load task: ${studentResp.code()} ${studentResp.message()}")
            }
        }
    }

    override suspend fun getTaskProgress(taskId: String): Flow<TaskProgressResponse> = flow {
        suspend fun fetch(): TaskProgressResponse? {
            val response = taskService.getTaskProgress(taskId)
            if (response.isSuccessful) {
                val raw = response.body()
                if (raw != null) {
                    // Derive progress and completion if missing using backend fields
                    val flags = listOfNotNull(raw.lessonCompleted, raw.exerciseCompleted, raw.quizCompleted)
                    val derivedProgress: Float? = if (flags.isNotEmpty()) {
                        flags.count { it }.toFloat() / flags.size
                    } else if (raw.maxQuizScore != null && raw.maxQuizScore > 0 && raw.quizScore != null) {
                        (raw.quizScore.toFloat() / raw.maxQuizScore.toFloat()).coerceIn(0f, 1f)
                    } else raw.progress
                    val derivedCompleted = (raw.lessonCompleted == true) && (raw.exerciseCompleted == true) && (raw.quizCompleted == true)

                    return raw.copy(
                        progress = derivedProgress,
                        isCompleted = derivedCompleted
                    )
                }
                return null
            }
            // Return null on specific errors to trigger fallback
            if (response.code() == 401 || response.code() == 403 || response.code() == 404) return null
            throw IllegalStateException("Failed to load task progress: ${response.code()} ${response.message()}")
        }

        val first = fetch()
        if (first != null) {
            emit(first)
            return@flow
        }

        // Fallback: create TaskCompletion then retry once
        val studentId = authTokenManager.physicalIdFlow.firstOrNull()
            ?: throw IllegalStateException("Missing current user ID; cannot create task completion")
        val createResp = taskService.createTaskCompletion(taskId, studentId)
        if (!createResp.isSuccessful && createResp.code() !in listOf(200, 201, 204, 409)) {
            // 409 or any non-fatal code can be ignored (record might already exist)
            // For other errors, continue to retry GET anyway
        }

        val second = fetch()
        if (second != null) {
            emit(second)
        } else {
            throw IllegalStateException("Failed to load task progress after creating completion")
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