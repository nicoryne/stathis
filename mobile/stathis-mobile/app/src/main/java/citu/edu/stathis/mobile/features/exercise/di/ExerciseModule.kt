package citu.edu.stathis.mobile.features.exercise.di

import citu.edu.stathis.mobile.features.exercise.data.ExerciseRepository
import citu.edu.stathis.mobile.features.exercise.data.ExerciseRepositoryImpl
import citu.edu.stathis.mobile.features.exercise.domain.ExerciseApiService
import citu.edu.stathis.mobile.features.vitals.domain.usecase.GetCurrentUserIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExerciseModule {

    @Provides
    @Singleton
    fun provideExerciseRepository(
        retrofit: Retrofit,
        getCurrentUserIdUseCase: GetCurrentUserIdUseCase
    ): ExerciseRepository {
        return ExerciseRepositoryImpl(
            apiService = retrofit.create<ExerciseApiService>(ExerciseApiService::class.java),
            getCurrentUserIdUseCase = getCurrentUserIdUseCase
        )
    }
}