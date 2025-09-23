package citu.edu.stathis.mobile.features.classroom.domain.usecase

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.domain.repository.ClassroomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for enrolling a student in a classroom
 */
class EnrollInClassroomUseCase @Inject constructor(
    private val classroomRepository: ClassroomRepository
) {
    /**
     * Executes the use case to enroll in a classroom
     * @param classroomCode The code of the classroom to enroll in
     */
    suspend operator fun invoke(classroomCode: String): Flow<Classroom> {
        return classroomRepository.enrollInClassroom(classroomCode)
    }
}
