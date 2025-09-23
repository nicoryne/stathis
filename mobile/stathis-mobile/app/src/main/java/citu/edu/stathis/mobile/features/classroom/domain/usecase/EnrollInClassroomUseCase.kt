package citu.edu.stathis.mobile.features.classroom.domain.usecase

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.domain.repository.ClassroomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EnrollInClassroomUseCase @Inject constructor(
    private val classroomRepository: ClassroomRepository
) {
    suspend operator fun invoke(classroomCode: String): Flow<Classroom> =
        classroomRepository.enrollInClassroom(classroomCode)
}
