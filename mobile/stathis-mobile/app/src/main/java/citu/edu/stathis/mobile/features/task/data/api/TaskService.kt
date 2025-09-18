package citu.edu.stathis.mobile.features.task.data.api

import citu.edu.stathis.mobile.features.task.domain.model.Task
import citu.edu.stathis.mobile.features.task.domain.model.TaskStatus
import retrofit2.Response
import retrofit2.http.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Retrofit service interface for task-related API endpoints
 */
interface TaskService {
    /**
     * Get all tasks for a student
     * @param studentId The ID of the student
     * @return Response containing a list of tasks
     */
    @GET("tasks/student/{studentId}")
    suspend fun getTasksForStudent(
        @Path("studentId") studentId: String
    ): Response<List<Task>>
    
    /**
     * Get tasks for a student in a specific classroom
     * @param studentId The ID of the student
     * @param classroomId The ID of the classroom
     * @return Response containing a list of tasks
     */
    @GET("tasks/student/{studentId}/classroom/{classroomId}")
    suspend fun getTasksForStudentInClassroom(
        @Path("studentId") studentId: String,
        @Path("classroomId") classroomId: String
    ): Response<List<Task>>
    
    /**
     * Get upcoming tasks for a student
     * @param studentId The ID of the student
     * @param limit Optional limit for the number of tasks to return
     * @return Response containing a list of tasks
     */
    @GET("tasks/student/{studentId}/upcoming")
    suspend fun getUpcomingTasks(
        @Path("studentId") studentId: String,
        @Query("limit") limit: Int?
    ): Response<List<Task>>
    
    /**
     * Get tasks due on a specific date for a student
     * @param studentId The ID of the student
     * @param date The date to get tasks for
     * @return Response containing a list of tasks
     */
    @GET("tasks/student/{studentId}/due")
    suspend fun getTasksDueOnDate(
        @Path("studentId") studentId: String,
        @Query("date") date: LocalDate
    ): Response<List<Task>>
    
    /**
     * Update the status of a task
     * @param taskId The ID of the task
     * @param status The new status of the task
     * @return Response indicating success or failure
     */
    @PATCH("tasks/{taskId}/status")
    suspend fun updateTaskStatus(
        @Path("taskId") taskId: String,
        @Body status: TaskStatus
    ): Response<Unit>
    
    /**
     * Get a task by its ID
     * @param taskId The ID of the task
     * @return Response containing the task
     */
    @GET("tasks/{taskId}")
    suspend fun getTaskById(
        @Path("taskId") taskId: String
    ): Response<Task>
    
    /**
     * Get overdue tasks for a student
     * @param studentId The ID of the student
     * @return Response containing a list of tasks
     */
    @GET("tasks/student/{studentId}/overdue")
    suspend fun getOverdueTasks(
        @Path("studentId") studentId: String
    ): Response<List<Task>>
    
    /**
     * Get completed tasks for a student
     * @param studentId The ID of the student
     * @param since Optional date to get tasks completed since
     * @return Response containing a list of tasks
     */
    @GET("tasks/student/{studentId}/completed")
    suspend fun getCompletedTasks(
        @Path("studentId") studentId: String,
        @Query("since") since: LocalDateTime?
    ): Response<List<Task>>
}
