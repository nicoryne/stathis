package citu.edu.stathis.mobile.features.tasks.data.api

import citu.edu.stathis.mobile.features.tasks.data.model.ScoreResponse
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import citu.edu.stathis.mobile.features.tasks.data.model.TaskProgressResponse
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

    @GET("api/student/tasks/{taskId}/progress")
    suspend fun getTaskProgress(
        @Path("taskId") taskId: String
    ): Response<TaskProgressResponse>

    @POST("api/student/tasks/{taskId}/quiz/{quizTemplateId}/score")
    suspend fun submitQuizScore(
        @Path("taskId") taskId: String,
        @Path("quizTemplateId") quizTemplateId: String,
        @Body score: Int
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
} 