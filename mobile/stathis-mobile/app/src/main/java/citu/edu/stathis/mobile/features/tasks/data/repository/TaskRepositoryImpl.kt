package citu.edu.stathis.mobile.features.tasks.data.repository

import citu.edu.stathis.mobile.features.tasks.data.api.TaskService
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.model.ScoreResponse
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskService: TaskService
) : TaskRepository {

    override suspend fun getStudentTasksForClassroom(classroomId: String): Flow<List<Task>> = flow {
        val response = taskService.getStudentTasksForClassroom(classroomId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
        }
    }

    override suspend fun getStudentTask(taskId: String): Flow<Task> = flow {
        val response = taskService.getStudentTask(taskId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
        }
    }

    override suspend fun getTaskProgress(taskId: String): Flow<TaskProgressResponse> = flow {
        val response = taskService.getTaskProgress(taskId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
        }
    }

    override suspend fun getLessonTemplate(lessonTemplateId: String): Flow<LessonTemplate> = flow {
        val response = taskService.getLessonTemplate(lessonTemplateId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
        }
    }

    override suspend fun getQuizTemplate(quizTemplateId: String): Flow<QuizTemplate> = flow {
        val response = taskService.getQuizTemplate(quizTemplateId)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
        }
    }

    override suspend fun submitQuizScore(
        taskId: String,
        quizTemplateId: String,
        score: Int
    ): Flow<ScoreResponse> = flow {
        val response = taskService.submitQuizScore(taskId, quizTemplateId, score)
        if (response.isSuccessful) {
            response.body()?.let { emit(it) }
        }
    }

    override suspend fun completeLesson(taskId: String, lessonTemplateId: String) {
        taskService.completeLesson(taskId, lessonTemplateId)
    }

    override suspend fun completeExercise(taskId: String, exerciseTemplateId: String) {
        taskService.completeExercise(taskId, exerciseTemplateId)
    }
} 