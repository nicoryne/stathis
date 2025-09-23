package citu.edu.stathis.mobile.features.exercise.di

import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.features.exercise.data.ExerciseRepository
import citu.edu.stathis.mobile.features.exercise.domain.repository.PerformanceRepository
import citu.edu.stathis.mobile.features.exercise.domain.usecase.AnalyzePostureWithBackendUseCase
import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetAvailableExercisesUseCase
import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetCurrentUserIdUseCase
import citu.edu.stathis.mobile.features.exercise.domain.usecase.SaveExerciseSessionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExerciseUseCaseModule {
    
    @Provides
    @Singleton
    fun provideGetAvailableExercisesUseCase(
        repository: ExerciseRepository
    ): GetAvailableExercisesUseCase {
        return GetAvailableExercisesUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideAnalyzePostureWithBackendUseCase(
        repository: ExerciseRepository
    ): AnalyzePostureWithBackendUseCase {
        return AnalyzePostureWithBackendUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideSaveExerciseSessionUseCase(
        repository: ExerciseRepository
    ): SaveExerciseSessionUseCase {
        return SaveExerciseSessionUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideGetCurrentUserIdUseCase(
        authRepository: AuthRepository
    ): GetCurrentUserIdUseCase {
        return GetCurrentUserIdUseCase(authRepository)
    }
    
}
