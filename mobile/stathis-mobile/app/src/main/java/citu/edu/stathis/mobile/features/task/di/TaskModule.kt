package citu.edu.stathis.mobile.features.task.di

import citu.edu.stathis.mobile.features.task.data.api.TaskService
import citu.edu.stathis.mobile.features.task.data.repository.TaskRepositoryImpl
import citu.edu.stathis.mobile.features.task.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Dagger Hilt module for the task feature
 */
@Module
@InstallIn(SingletonComponent::class)
object TaskModule {
    
    /**
     * Provides the TaskService API interface
     */
    @Provides
    @Singleton
    fun provideTaskService(retrofit: Retrofit): TaskService {
        return retrofit.create(TaskService::class.java)
    }
    
    /**
     * Provides the TaskRepository implementation
     */
    @Provides
    @Singleton
    fun provideTaskRepository(taskService: TaskService): TaskRepository {
        return TaskRepositoryImpl(taskService)
    }
}
