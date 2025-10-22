package citu.edu.stathis.mobile.features.tasks.di

import citu.edu.stathis.mobile.features.tasks.data.repository.TaskRepository
import citu.edu.stathis.mobile.features.tasks.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskUseCaseModule {

    @Provides
    @Singleton
    fun provideGetTasksForClassroomUseCase(repository: TaskRepository): GetTasksForClassroomUseCase =
        GetTasksForClassroomUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTasksForClassroomResultUseCase(repository: TaskRepository): GetTasksForClassroomResultUseCase =
        GetTasksForClassroomResultUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTaskDetailsUseCase(repository: TaskRepository): GetTaskDetailsUseCase =
        GetTaskDetailsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTaskDetailsResultUseCase(repository: TaskRepository): GetTaskDetailsResultUseCase =
        GetTaskDetailsResultUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTaskProgressUseCase(repository: TaskRepository): GetTaskProgressUseCase =
        GetTaskProgressUseCase(repository)

    @Provides
    @Singleton
    fun provideGetTaskProgressResultUseCase(repository: TaskRepository): GetTaskProgressResultUseCase =
        GetTaskProgressResultUseCase(repository)

    @Provides
    @Singleton
    fun provideSubmitQuizScoreUseCase(repository: TaskRepository): SubmitQuizScoreUseCase =
        SubmitQuizScoreUseCase(repository)

    @Provides
    @Singleton
    fun provideSubmitQuizScoreResultUseCase(repository: TaskRepository): SubmitQuizScoreResultUseCase =
        SubmitQuizScoreResultUseCase(repository)

    @Provides
    @Singleton
    fun provideCompleteLessonUseCase(repository: TaskRepository): CompleteLessonUseCase =
        CompleteLessonUseCase(repository)

    @Provides
    @Singleton
    fun provideCompleteLessonResultUseCase(repository: TaskRepository): CompleteLessonResultUseCase =
        CompleteLessonResultUseCase(repository)

    @Provides
    @Singleton
    fun provideCompleteExerciseUseCase(repository: TaskRepository): CompleteExerciseUseCase =
        CompleteExerciseUseCase(repository)

    @Provides
    @Singleton
    fun provideCompleteExerciseResultUseCase(repository: TaskRepository): CompleteExerciseResultUseCase =
        CompleteExerciseResultUseCase(repository)

    @Provides
    @Singleton
    fun provideGetQuizScoreUseCase(repository: TaskRepository): GetQuizScoreUseCase =
        GetQuizScoreUseCase(repository)

    @Provides
    @Singleton
    fun provideGetQuizScoreResultUseCase(repository: TaskRepository): GetQuizScoreResultUseCase =
        GetQuizScoreResultUseCase(repository)

    @Provides
    @Singleton
    fun provideGetScoresByStudentAndTaskUseCase(repository: TaskRepository): GetScoresByStudentAndTaskUseCase =
        GetScoresByStudentAndTaskUseCase(repository)

    @Provides
    @Singleton
    fun provideGetScoresByStudentAndTaskResultUseCase(repository: TaskRepository): GetScoresByStudentAndTaskResultUseCase =
        GetScoresByStudentAndTaskResultUseCase(repository)
}


