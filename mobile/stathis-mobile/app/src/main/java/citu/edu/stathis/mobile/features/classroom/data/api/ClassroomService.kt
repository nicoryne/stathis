package citu.edu.stathis.mobile.features.classroom.data.api

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.data.model.ClassroomProgress
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import retrofit2.Response
import retrofit2.http.*

interface ClassroomService {
    // Matches swagger: /api/classrooms/student
    @GET("api/classrooms/student")
    suspend fun getStudentClassrooms(): Response<List<Classroom>>

    // Matches swagger: /api/classrooms/{physicalId}
    @GET("api/classrooms/{classroomId}")
    suspend fun getStudentClassroom(
        @Path("classroomId") classroomId: String
    ): Response<Classroom>

    // Matches swagger: /api/classrooms/enroll (body is a map of strings)
    @POST("api/classrooms/enroll")
    suspend fun enrollInClassroom(
        @Body classroomCode: Map<String, String>
    ): Response<Unit>

    // Matches swagger: /api/tasks/classroom/{classroomId}
    @GET("api/tasks/classroom/{classroomId}")
    suspend fun getClassroomTasks(
        @Path("classroomId") classroomId: String
    ): Response<List<Task>>

    // No direct progress endpoint in swagger per classroom for student; keep placeholder if used elsewhere
    @GET("api/classrooms/{classroomId}")
    suspend fun getClassroomProgress(
        @Path("classroomId") classroomId: String
    ): Response<ClassroomProgress>
}