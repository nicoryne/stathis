package citu.edu.stathis.mobile.features.tasks.data.repository

import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.model.ScoreResponse
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.ExerciseTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizSubmission
import citu.edu.stathis.mobile.features.tasks.data.model.QuizAutoCheckRequest
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun getStudentTasksForClassroom(classroomId: String): Flow<List<Task>>
    suspend fun getStudentTask(taskId: String): Flow<Task>
    suspend fun getTaskProgress(taskId: String): Flow<TaskProgressResponse>
    suspend fun getLessonTemplate(lessonTemplateId: String): Flow<LessonTemplate>
    suspend fun getQuizTemplate(quizTemplateId: String): Flow<QuizTemplate>
    suspend fun getExerciseTemplate(exerciseTemplateId: String): Flow<ExerciseTemplate>
    suspend fun submitQuizScore(taskId: String, quizTemplateId: String, score: Int): Flow<ScoreResponse>
    suspend fun autoCheckQuiz(taskId: String, quizTemplateId: String, request: QuizAutoCheckRequest): Flow<ScoreResponse>
    suspend fun completeLesson(taskId: String, lessonTemplateId: String)
    suspend fun completeExercise(taskId: String, exerciseTemplateId: String)
    suspend fun getQuizScore(studentId: String, taskId: String, quizTemplateId: String): Flow<ScoreResponse>
    suspend fun getScoresByStudentAndTask(studentId: String, taskId: String): Flow<List<ScoreResponse>>
} 