package citu.edu.stathis.mobile.features.classroom.domain.usecase

import citu.edu.stathis.mobile.features.classroom.domain.repository.ClassroomRepository
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all tasks for a specific classroom
 */
class GetClassroomTasksUseCase @Inject constructor(
    private val classroomRepository: ClassroomRepository
) {
    /**
     * Executes the use case to retrieve all tasks for a classroom
     * @param classroomId The ID of the classroom to get tasks for
     */
    suspend operator fun invoke(classroomId: String): Flow<List<Task>> {
        return classroomRepository.getClassroomTasks(classroomId)
    }
}
