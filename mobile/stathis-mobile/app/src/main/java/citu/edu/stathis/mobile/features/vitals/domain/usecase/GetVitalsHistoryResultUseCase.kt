package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.domain.repository.VitalsRepository
import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetCurrentUserIdUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetVitalsHistoryResultUseCase @Inject constructor(
    private val vitalsRepository: VitalsRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(): Flow<Result<List<VitalSigns>>> {
        val currentUserId = getCurrentUserIdUseCase()
            ?: return kotlinx.coroutines.flow.flow { emit(Result.Error("User not identified.")) }
        return vitalsRepository.getVitalsHistory(currentUserId)
            .map { client -> if (client.success && client.data != null) Result.Success(client.data) else Result.Error(client.message) }
    }
}


