package citu.edu.stathis.mobile.features.classroom.di

import citu.edu.stathis.mobile.features.classroom.data.api.ClassroomService
import citu.edu.stathis.mobile.features.classroom.data.repository.ClassroomRepository
import citu.edu.stathis.mobile.features.classroom.data.repository.ClassroomRepositoryImpl
import citu.edu.stathis.mobile.features.classroom.data.adapter.ClassroomRepositoryAdapter
import citu.edu.stathis.mobile.features.classroom.domain.repository.ClassroomRepository as DomainClassroomRepository
import citu.edu.stathis.mobile.features.classroom.domain.usecase.EnrollInClassroomUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetClassroomDetailsUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetClassroomProgressUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetClassroomTasksUseCase
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetStudentClassroomsResultUseCase
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

    @Binds
    @Singleton
    abstract fun bindDomainClassroomRepository(
        adapter: ClassroomRepositoryAdapter
    ): DomainClassroomRepository

    companion object {
        @Provides
        @Singleton
        fun provideClassroomService(retrofit: Retrofit): ClassroomService {
            return retrofit.create(ClassroomService::class.java)
        }

        @Provides
        @Singleton
        fun provideGetStudentClassroomsResultUseCase(
            classroomRepository: DomainClassroomRepository
        ): GetStudentClassroomsResultUseCase = GetStudentClassroomsResultUseCase(classroomRepository)

        @Provides
        @Singleton
        fun provideEnrollInClassroomUseCase(
            classroomRepository: DomainClassroomRepository
        ): EnrollInClassroomUseCase = EnrollInClassroomUseCase(classroomRepository)

        @Provides
        @Singleton
        fun provideGetClassroomDetailsUseCase(
            classroomRepository: DomainClassroomRepository
        ): GetClassroomDetailsUseCase = GetClassroomDetailsUseCase(classroomRepository)

        @Provides
        @Singleton
        fun provideGetClassroomProgressUseCase(
            classroomRepository: DomainClassroomRepository
        ): GetClassroomProgressUseCase = GetClassroomProgressUseCase(classroomRepository)

        @Provides
        @Singleton
        fun provideGetClassroomTasksUseCase(
            classroomRepository: DomainClassroomRepository
        ): GetClassroomTasksUseCase = GetClassroomTasksUseCase(classroomRepository)
    }
} 