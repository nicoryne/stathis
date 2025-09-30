package citu.edu.stathis.mobile.features.vitals.di

import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetCurrentUserIdUseCase
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.vitals.domain.repository.VitalsRepository
import citu.edu.stathis.mobile.features.vitals.domain.usecase.DeleteVitalRecordUseCase
import citu.edu.stathis.mobile.features.vitals.domain.usecase.GetVitalsHistoryUseCase
import citu.edu.stathis.mobile.features.vitals.domain.usecase.SaveVitalsUseCase
import citu.edu.stathis.mobile.features.vitals.domain.usecase.GetVitalsHistoryResultUseCase
import citu.edu.stathis.mobile.features.vitals.domain.usecase.SaveVitalsResultUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VitalsUseCaseModule {
    
    @Provides
    @Singleton
    fun provideGetVitalsHistoryUseCase(
        repository: VitalsRepository,
        getCurrentUserIdUseCase: GetCurrentUserIdUseCase
    ): GetVitalsHistoryUseCase {
        return GetVitalsHistoryUseCase(repository, getCurrentUserIdUseCase)
    }
    
    @Provides
    @Singleton
    fun provideSaveVitalsUseCase(
        repository: VitalsRepository,
        getCurrentUserIdUseCase: GetCurrentUserIdUseCase
    ): SaveVitalsUseCase {
        return SaveVitalsUseCase(repository, getCurrentUserIdUseCase)
    }

    @Provides
    @Singleton
    fun provideGetVitalsHistoryResultUseCase(
        repository: VitalsRepository,
        getCurrentUserIdUseCase: GetCurrentUserIdUseCase
    ): GetVitalsHistoryResultUseCase {
        return GetVitalsHistoryResultUseCase(repository, getCurrentUserIdUseCase)
    }

    @Provides
    @Singleton
    fun provideSaveVitalsResultUseCase(
        repository: VitalsRepository,
        getCurrentUserIdUseCase: GetCurrentUserIdUseCase
    ): SaveVitalsResultUseCase {
        return SaveVitalsResultUseCase(repository, getCurrentUserIdUseCase)
    }
    
    @Provides
    @Singleton
    fun provideDeleteVitalRecordUseCase(
        repository: VitalsRepository
    ): DeleteVitalRecordUseCase {
        return DeleteVitalRecordUseCase(repository)
    }
    
}
