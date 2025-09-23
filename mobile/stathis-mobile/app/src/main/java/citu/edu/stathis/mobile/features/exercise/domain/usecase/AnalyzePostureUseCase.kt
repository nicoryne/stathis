package citu.edu.stathis.mobile.features.exercise.domain.usecase

import citu.edu.stathis.mobile.features.exercise.domain.model.PostureAnalysis
import citu.edu.stathis.mobile.features.exercise.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnalyzePostureUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    operator fun invoke(exerciseId: String): Flow<PostureAnalysis> {
        return repository.analyzePostureInRealTime(exerciseId)
    }
} 