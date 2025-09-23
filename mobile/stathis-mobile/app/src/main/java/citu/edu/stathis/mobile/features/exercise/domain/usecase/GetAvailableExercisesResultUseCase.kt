package citu.edu.stathis.mobile.features.exercise.domain.usecase

import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.common.domain.toResult
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.domain.repository.ExerciseRepository
import javax.inject.Inject

class GetAvailableExercisesResultUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(): Result<List<Exercise>> {
        return repository.getAvailableExercises().toResult()
    }
}


