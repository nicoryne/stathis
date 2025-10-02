package citu.edu.stathis.mobile.features.exercise.domain.usecase

import citu.edu.stathis.mobile.features.exercise.data.remote.dto.ClassificationResultDto
import citu.edu.stathis.mobile.features.exercise.domain.repository.ExerciseRepository
import javax.inject.Inject

class ClassifyPoseUseCase @Inject constructor(
    private val repo: ExerciseRepository
) {
    suspend operator fun invoke(window: Array<Array<FloatArray>>): ClassificationResultDto =
        repo.classify(window)
}


