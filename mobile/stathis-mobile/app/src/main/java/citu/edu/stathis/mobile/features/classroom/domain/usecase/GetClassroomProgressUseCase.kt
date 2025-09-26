package citu.edu.stathis.mobile.features.classroom.domain.usecase

import citu.edu.stathis.mobile.features.classroom.data.model.ClassroomProgress
import citu.edu.stathis.mobile.features.classroom.domain.repository.ClassroomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetClassroomProgressUseCase @Inject constructor(
    private val classroomRepository: ClassroomRepository
) {
    suspend operator fun invoke(classroomId: String): Flow<ClassroomProgress> {
        return classroomRepository.getClassroomProgress(classroomId)
    }
}


