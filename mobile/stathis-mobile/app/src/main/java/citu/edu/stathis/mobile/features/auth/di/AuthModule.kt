package citu.edu.stathis.mobile.features.auth.di

import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepositoryImpl
import citu.edu.stathis.mobile.features.auth.domain.AuthApiService
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService,
        authTokenManager: AuthTokenManager
    ): AuthRepository {
        return AuthRepositoryImpl(authApiService, authTokenManager)
    }
}