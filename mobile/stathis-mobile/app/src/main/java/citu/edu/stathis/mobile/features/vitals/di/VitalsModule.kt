package citu.edu.stathis.mobile.features.vitals.di

import citu.edu.stathis.mobile.features.vitals.domain.VitalsApiService
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepositoryImpl
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VitalsRepositoryModule { // Renamed for clarity since it binds repository

    @Binds
    @Singleton
    abstract fun bindVitalsRepository(
        vitalsRepositoryImpl: VitalsRepositoryImpl
    ): VitalsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object VitalsNetworkModule { // Separate module for network specific provisions


    @Provides
    @Singleton
    fun provideVitalsApiService(retrofit: Retrofit): VitalsApiService {
        return retrofit.create(VitalsApiService::class.java)
    }
}

// Use cases will have @Inject constructor and be provided by Hilt automatically.