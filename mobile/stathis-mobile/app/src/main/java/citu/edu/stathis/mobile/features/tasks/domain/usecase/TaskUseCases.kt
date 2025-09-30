package citu.edu.stathis.mobile.features.tasks.domain.usecase

import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.common.domain.safeCall
import citu.edu.stathis.mobile.features.tasks.data.model.ScoreResponse
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import citu.edu.stathis.mobile.features.tasks.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetTasksForClassroomUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(classroomId: String): Flow<List<Task>> =
        repository.getStudentTasksForClassroom(classroomId)
}

class GetTaskDetailsUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Flow<Task> =
        repository.getStudentTask(taskId)
}

class GetTaskProgressUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Flow<TaskProgressResponse> =
        repository.getTaskProgress(taskId)
}

class GetLessonTemplateUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(lessonTemplateId: String): Flow<LessonTemplate> =
        repository.getLessonTemplate(lessonTemplateId)
}

class GetQuizTemplateUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(quizTemplateId: String): Flow<QuizTemplate> =
        repository.getQuizTemplate(quizTemplateId)
}

// Result-wrapped variants for UI simplicity and consistency across features
class GetTasksForClassroomResultUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(classroomId: String): Result<List<Task>> =
        safeCall { repository.getStudentTasksForClassroom(classroomId).first() }
}

class GetTaskDetailsResultUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<Task> =
        safeCall { repository.getStudentTask(taskId).first() }
}

class GetTaskProgressResultUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<TaskProgressResponse> =
        safeCall { repository.getTaskProgress(taskId).first() }
}

class GetLessonTemplateResultUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(lessonTemplateId: String): Result<LessonTemplate> =
        safeCall { repository.getLessonTemplate(lessonTemplateId).first() }
}

class GetQuizTemplateResultUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(quizTemplateId: String): Result<QuizTemplate> =
        safeCall { repository.getQuizTemplate(quizTemplateId).first() }
}

class SubmitQuizScoreUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, quizTemplateId: String, score: Int): Flow<ScoreResponse> =
        repository.submitQuizScore(taskId, quizTemplateId, score)
}

class SubmitQuizScoreResultUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, quizTemplateId: String, score: Int): Result<ScoreResponse> =
        safeCall { repository.submitQuizScore(taskId, quizTemplateId, score).first() }
}

class CompleteLessonUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, lessonTemplateId: String) =
        repository.completeLesson(taskId, lessonTemplateId)
}

class CompleteLessonResultUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, lessonTemplateId: String): Result<Unit> =
        try {
            repository.completeLesson(taskId, lessonTemplateId)
            Result.Success(Unit)
        } catch (e: Throwable) {
            Result.Error(e.message ?: "Unknown error", e, null)
        }
}

class CompleteExerciseUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, exerciseTemplateId: String) =
        repository.completeExercise(taskId, exerciseTemplateId)
}

class CompleteExerciseResultUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, exerciseTemplateId: String): Result<Unit> =
        try {
            repository.completeExercise(taskId, exerciseTemplateId)
            Result.Success(Unit)
        } catch (e: Throwable) {
            Result.Error(e.message ?: "Unknown error", e, null)
        }
}

