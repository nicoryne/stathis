package citu.edu.stathis.mobile.features.classroom.data.api

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.data.model.ClassroomProgress
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import retrofit2.Response
import retrofit2.http.*

interface ClassroomService {
    @GET("api/student/classrooms")
    suspend fun getStudentClassrooms(): Response<List<Classroom>>

    @GET("api/student/classrooms/{classroomId}")
    suspend fun getStudentClassroom(
        @Path("classroomId") classroomId: String
    ): Response<Classroom>

    @POST("api/student/classrooms/enroll")
    suspend fun enrollInClassroom(
        @Body classroomCode: String
    ): Response<Classroom>

    @GET("api/student/classrooms/{classroomId}/tasks")
    suspend fun getClassroomTasks(
        @Path("classroomId") classroomId: String
    ): Response<List<Task>>

    @GET("api/student/classrooms/{classroomId}/progress")
    suspend fun getClassroomProgress(
        @Path("classroomId") classroomId: String
    ): Response<ClassroomProgress>
} 