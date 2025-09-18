package citu.edu.stathis.mobile.features.task.data.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.task.data.api.TaskService
import citu.edu.stathis.mobile.features.task.domain.model.Task
import citu.edu.stathis.mobile.features.task.domain.model.TaskStatus
import citu.edu.stathis.mobile.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the TaskRepository interface
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskService: TaskService
) : TaskRepository {

    override fun getTasksForStudent(studentId: String): Flow<ClientResponse<List<Task>>> = flow {
        try {
            val response = taskService.getTasksForStudent(studentId)
            if (response.isSuccessful) {
                val tasks: List<Task> = response.body() ?: emptyList()
                emit(ClientResponse(success = true, data = tasks, message = "Tasks retrieved successfully"))
            } else {
                emit(ClientResponse<List<Task>>(
                    success = false,
                    data = null,
                    message = "Failed to retrieve tasks: HTTP ${response.code()}"
                ))
            }
        } catch (e: Exception) {
            emit(ClientResponse<List<Task>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTasksForStudentInClassroom(studentId: String, classroomId: String): Flow<ClientResponse<List<Task>>> = flow {
        try {
            val response = taskService.getTasksForStudentInClassroom(studentId, classroomId)
            if (response.isSuccessful) {
                val tasks: List<Task> = response.body() ?: emptyList()
                emit(ClientResponse(success = true, data = tasks, message = "Classroom tasks retrieved successfully"))
            } else {
                emit(ClientResponse<List<Task>>(
                    success = false,
                    data = null,
                    message = "Failed to retrieve classroom tasks: HTTP ${response.code()}"
                ))
            }
        } catch (e: Exception) {
            emit(ClientResponse<List<Task>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getUpcomingTasks(studentId: String, limit: Int?): Flow<ClientResponse<List<Task>>> = flow {
        try {
            val response = taskService.getUpcomingTasks(studentId, limit)
            if (response.isSuccessful) {
                val tasks: List<Task> = response.body() ?: emptyList()
                emit(ClientResponse(success = true, data = tasks, message = "Upcoming tasks retrieved successfully"))
            } else {
                emit(ClientResponse<List<Task>>(
                    success = false,
                    data = null,
                    message = "Failed to retrieve upcoming tasks: HTTP ${response.code()}"
                ))
            }
        } catch (e: Exception) {
            emit(ClientResponse<List<Task>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTasksDueToday(studentId: String): Flow<ClientResponse<List<Task>>> = flow {
        try {
            val today = LocalDate.now()
            val response = taskService.getTasksDueOnDate(studentId, today)
            if (response.isSuccessful) {
                val tasks: List<Task> = response.body() ?: emptyList()
                emit(ClientResponse(success = true, data = tasks, message = "Today's tasks retrieved successfully"))
            } else {
                emit(ClientResponse<List<Task>>(
                    success = false,
                    data = null,
                    message = "Failed to retrieve today's tasks: HTTP ${response.code()}"
                ))
            }
        } catch (e: Exception) {
            emit(ClientResponse<List<Task>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): ClientResponse<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = taskService.updateTaskStatus(taskId, status)
            if (response.isSuccessful) {
                ClientResponse(success = true, data = Unit, message = "Task status updated successfully")
            } else {
                ClientResponse(
                    success = false,
                    data = null,
                    message = "Failed to update task status: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ClientResponse(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun getTaskById(taskId: String): ClientResponse<Task> = withContext(Dispatchers.IO) {
        try {
            val response = taskService.getTaskById(taskId)
            if (response.isSuccessful) {
                val task = response.body()
                if (task != null) {
                    ClientResponse(success = true, data = task, message = "Task retrieved successfully")
                } else {
                    ClientResponse(success = false, data = null, message = "Task not found")
                }
            } else {
                ClientResponse(
                    success = false,
                    data = null,
                    message = "Failed to retrieve task: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ClientResponse(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}")
        }
    }

    override fun getOverdueTasks(studentId: String): Flow<ClientResponse<List<Task>>> = flow {
        try {
            val response = taskService.getOverdueTasks(studentId)
            if (response.isSuccessful) {
                val tasks: List<Task> = response.body() ?: emptyList()
                emit(ClientResponse(success = true, data = tasks, message = "Overdue tasks retrieved successfully"))
            } else {
                emit(ClientResponse<List<Task>>(
                    success = false,
                    data = null,
                    message = "Failed to retrieve overdue tasks: HTTP ${response.code()}"
                ))
            }
        } catch (e: Exception) {
            emit(ClientResponse<List<Task>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getCompletedTasks(studentId: String, since: LocalDateTime?): Flow<ClientResponse<List<Task>>> = flow {
        try {
            val response = taskService.getCompletedTasks(studentId, since)
            if (response.isSuccessful) {
                val tasks: List<Task> = response.body() ?: emptyList()
                emit(ClientResponse(success = true, data = tasks, message = "Completed tasks retrieved successfully"))
            } else {
                emit(ClientResponse<List<Task>>(
                    success = false,
                    data = null,
                    message = "Failed to retrieve completed tasks: HTTP ${response.code()}"
                ))
            }
        } catch (e: Exception) {
            emit(ClientResponse<List<Task>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)
}
