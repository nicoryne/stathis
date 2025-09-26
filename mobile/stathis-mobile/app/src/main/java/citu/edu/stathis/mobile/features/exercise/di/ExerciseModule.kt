package citu.edu.stathis.mobile.features.exercise.di

import citu.edu.stathis.mobile.features.exercise.domain.repository.ExerciseRepository
import citu.edu.stathis.mobile.features.exercise.data.repository.ExerciseRepositoryImpl
import citu.edu.stathis.mobile.features.exercise.domain.ExerciseApiService
import citu.edu.stathis.mobile.features.exercise.data.datasource.ExerciseApi
import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetCurrentUserIdUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExerciseModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    companion object {
        @Provides
        @Singleton
        fun provideExerciseApiService(retrofit: Retrofit): ExerciseApiService {
            return retrofit.create(ExerciseApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideExerciseApi(retrofit: Retrofit): ExerciseApi {
            return retrofit.create(ExerciseApi::class.java)
        }
    }
}