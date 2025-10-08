package citu.edu.stathis.mobile.features.tasks.data.api

import citu.edu.stathis.mobile.features.tasks.data.model.ScoreResponse
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizSubmission
import citu.edu.stathis.mobile.features.tasks.data.model.QuizAutoCheckRequest
import retrofit2.Response
import retrofit2.http.*

interface TaskService {
    @GET("api/student/tasks/classroom/{classroomId}")
    suspend fun getStudentTasksForClassroom(
        @Path("classroomId") classroomId: String
    ): Response<List<Task>>

    @GET("api/student/tasks/{taskId}")
    suspend fun getStudentTask(
        @Path("taskId") taskId: String
    ): Response<Task>

    // Fallback to fetch full task details with teacher-defined fields like maxAttempts
    @GET("api/tasks/{physicalId}")
    suspend fun getTaskByPhysicalId(
        @Path("physicalId") physicalId: String
    ): Response<Task>

    @GET("api/student/tasks/{taskId}/progress")
    suspend fun getTaskProgress(
        @Path("taskId") taskId: String
    ): Response<TaskProgressResponse>

    @GET("api/templates/lessons/{lessonTemplateId}")
    suspend fun getLessonTemplate(
        @Path("lessonTemplateId") lessonTemplateId: String
    ): Response<LessonTemplate>

    @GET("api/templates/quizzes/{quizTemplateId}")
    suspend fun getQuizTemplate(
        @Path("quizTemplateId") quizTemplateId: String
    ): Response<QuizTemplate>

    @POST("api/student/tasks/{taskId}/quiz/{quizTemplateId}/score")
    suspend fun submitQuizScore(
        @Path("taskId") taskId: String,
        @Path("quizTemplateId") quizTemplateId: String,
        @Body score: Int
    ): Response<ScoreResponse>

    @POST("api/student/tasks/{taskId}/quiz/{quizTemplateId}/auto-check")
    suspend fun autoCheckQuiz(
        @Path("taskId") taskId: String,
        @Path("quizTemplateId") quizTemplateId: String,
        @Body submission: QuizAutoCheckRequest
    ): Response<ScoreResponse>

    @POST("api/student/tasks/{taskId}/lesson/{lessonTemplateId}/complete")
    suspend fun completeLesson(
        @Path("taskId") taskId: String,
        @Path("lessonTemplateId") lessonTemplateId: String
    ): Response<Unit>

    @POST("api/student/tasks/{taskId}/exercise/{exerciseTemplateId}/complete")
    suspend fun completeExercise(
        @Path("taskId") taskId: String,
        @Path("exerciseTemplateId") exerciseTemplateId: String
    ): Response<Unit>

    // Create a TaskCompletion record so progress queries don't 403/404 when missing
    @POST("api/v1/task-completions/{taskId}")
    suspend fun createTaskCompletion(
        @Path("taskId") taskId: String,
        @Query("studentId") studentId: String
    ): Response<Unit>
} 