package citu.edu.stathis.mobile.features.vitals.di

import citu.edu.stathis.mobile.features.vitals.domain.usecase.GetCurrentUserIdUseCase
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository
import citu.edu.stathis.mobile.features.vitals.domain.usecase.DeleteVitalRecordUseCase
import citu.edu.stathis.mobile.features.vitals.domain.usecase.GetVitalsHistoryUseCase
import citu.edu.stathis.mobile.features.vitals.domain.usecase.SaveVitalsUseCase
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
    fun provideDeleteVitalRecordUseCase(
        repository: VitalsRepository
    ): DeleteVitalRecordUseCase {
        return DeleteVitalRecordUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideGetCurrentUserIdUseCase(
        authTokenManager: AuthTokenManager,
        authRepository: AuthRepository
    ): GetCurrentUserIdUseCase {
        return GetCurrentUserIdUseCase(authTokenManager, authRepository)
    }
}
