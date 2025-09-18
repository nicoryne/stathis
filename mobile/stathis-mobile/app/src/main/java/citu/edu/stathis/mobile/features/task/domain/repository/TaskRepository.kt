package citu.edu.stathis.mobile.features.task.domain.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.task.domain.model.Task
import citu.edu.stathis.mobile.features.task.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository interface for managing tasks related to students and classrooms
 */
interface TaskRepository {
    /**
     * Get all tasks for a student
     * @param studentId The ID of the student
     * @return Flow of ClientResponse containing a list of tasks
     */
    fun getTasksForStudent(studentId: String): Flow<ClientResponse<List<Task>>>
    
    /**
     * Get tasks for a student in a specific classroom
     * @param studentId The ID of the student
     * @param classroomId The ID of the classroom
     * @return Flow of ClientResponse containing a list of tasks
     */
    fun getTasksForStudentInClassroom(studentId: String, classroomId: String): Flow<ClientResponse<List<Task>>>
    
    /**
     * Get upcoming tasks for a student
     * @param studentId The ID of the student
     * @param limit Optional limit for the number of tasks to return
     * @return Flow of ClientResponse containing a list of tasks
     */
    fun getUpcomingTasks(studentId: String, limit: Int? = null): Flow<ClientResponse<List<Task>>>
    
    /**
     * Get tasks due today for a student
     * @param studentId The ID of the student
     * @return Flow of ClientResponse containing a list of tasks
     */
    fun getTasksDueToday(studentId: String): Flow<ClientResponse<List<Task>>>
    
    /**
     * Update the status of a task
     * @param taskId The ID of the task
     * @param status The new status of the task
     * @return ClientResponse indicating success or failure
     */
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): ClientResponse<Unit>
    
    /**
     * Get a task by its ID
     * @param taskId The ID of the task
     * @return ClientResponse containing the task
     */
    suspend fun getTaskById(taskId: String): ClientResponse<Task>
    
    /**
     * Get overdue tasks for a student
     * @param studentId The ID of the student
     * @return Flow of ClientResponse containing a list of tasks
     */
    fun getOverdueTasks(studentId: String): Flow<ClientResponse<List<Task>>>
    
    /**
     * Get completed tasks for a student
     * @param studentId The ID of the student
     * @param since Optional date to get tasks completed since
     * @return Flow of ClientResponse containing a list of tasks
     */
    fun getCompletedTasks(studentId: String, since: LocalDateTime? = null): Flow<ClientResponse<List<Task>>>
}
