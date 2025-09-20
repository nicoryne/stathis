package citu.edu.stathis.mobile.features.classroom.di

import citu.edu.stathis.mobile.features.classroom.data.api.ClassroomService
import citu.edu.stathis.mobile.features.classroom.data.repository.ClassroomRepository
import citu.edu.stathis.mobile.features.classroom.data.repository.ClassroomRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClassroomModule {
    
    @Binds
    @Singleton
    abstract fun bindClassroomRepository(
        classroomRepositoryImpl: ClassroomRepositoryImpl
    ): ClassroomRepository

    companion object {
        @Provides
        @Singleton
        fun provideClassroomService(retrofit: Retrofit): ClassroomService {
            return retrofit.create(ClassroomService::class.java)
        }
    }
} 