package citu.edu.stathis.mobile.features.progress.di

import citu.edu.stathis.mobile.features.progress.data.api.ProgressService
import citu.edu.stathis.mobile.features.progress.data.repository.ProgressRepositoryImpl
import citu.edu.stathis.mobile.features.progress.domain.repository.ProgressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Dagger Hilt module for the progress tracking feature
 */
@Module
@InstallIn(SingletonComponent::class)
object ProgressModule {
    
    /**
     * Provides the ProgressService API interface
     */
    @Provides
    @Singleton
    fun provideProgressService(retrofit: Retrofit): ProgressService {
        return retrofit.create(ProgressService::class.java)
    }
    
    /**
     * Provides the ProgressRepository implementation
     */
    @Provides
    @Singleton
    fun provideProgressRepository(progressService: ProgressService): ProgressRepository {
        return ProgressRepositoryImpl(progressService)
    }
}
