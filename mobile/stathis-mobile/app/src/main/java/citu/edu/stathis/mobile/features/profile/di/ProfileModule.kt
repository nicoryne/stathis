package citu.edu.stathis.mobile.features.profile.di

import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.profile.data.repository.ProfileRepository
import citu.edu.stathis.mobile.features.profile.data.repository.ProfileRepositoryImpl
import citu.edu.stathis.mobile.features.profile.domain.ProfileApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService {
        return retrofit.create(ProfileApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        profileApiService: ProfileApiService,
        authTokenManager: AuthTokenManager
    ): ProfileRepository {
        return ProfileRepositoryImpl(profileApiService, authTokenManager)
    }
}