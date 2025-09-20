package citu.edu.stathis.mobile.di

import citu.edu.stathis.mobile.features.auth.domain.usecase.TokenValidationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideTokenValidationUseCase(): TokenValidationUseCase {
        return TokenValidationUseCase()
    }
} 