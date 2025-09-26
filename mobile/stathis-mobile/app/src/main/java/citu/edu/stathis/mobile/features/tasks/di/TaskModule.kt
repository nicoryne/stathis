package citu.edu.stathis.mobile.features.tasks.di

import citu.edu.stathis.mobile.features.tasks.data.api.TaskService
import citu.edu.stathis.mobile.features.tasks.data.repository.TaskRepository
import citu.edu.stathis.mobile.features.tasks.data.repository.TaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TaskModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    companion object {
        @Provides
        @Singleton
        fun provideTaskService(retrofit: Retrofit): TaskService {
            return retrofit.create(TaskService::class.java)
        }
    }
}