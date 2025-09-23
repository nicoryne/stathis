package citu.edu.stathis.mobile.features.classroom.domain.usecase

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.domain.repository.ClassroomRepository
import citu.edu.stathis.mobile.features.common.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case that wraps classroom listing in a domain Result flow (contained rollout).
 */
class GetStudentClassroomsResultUseCase @Inject constructor(
    private val classroomRepository: ClassroomRepository
) {
    suspend operator fun invoke(): Flow<Result<List<Classroom>>> =
        classroomRepository.getStudentClassrooms()
            .map { value -> Result.Success(value) as Result<List<Classroom>> }
            .catch { e -> emit(Result.Error(e.message ?: "Unknown error", e)) }
}


