package citu.edu.stathis.mobile.features.classroom.domain.usecase

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.domain.repository.ClassroomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all classrooms for the current student
 */
class GetStudentClassroomsUseCase @Inject constructor(
    private val classroomRepository: ClassroomRepository
) {
    /**
     * Executes the use case to retrieve all student classrooms
     */
    suspend operator fun invoke(): Flow<List<Classroom>> {
        return classroomRepository.getStudentClassrooms()
    }
}
