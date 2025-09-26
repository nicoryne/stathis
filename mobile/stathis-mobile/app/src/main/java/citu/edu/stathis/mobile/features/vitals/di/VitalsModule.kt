package citu.edu.stathis.mobile.features.vitals.di

import citu.edu.stathis.mobile.features.vitals.domain.VitalsApiService
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepositoryImpl
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository as DataVitalsRepository
import citu.edu.stathis.mobile.features.vitals.domain.repository.VitalsRepository as DomainVitalsRepository
import citu.edu.stathis.mobile.features.vitals.data.adapter.VitalsRepositoryAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VitalsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDataVitalsRepository(
        vitalsRepositoryImpl: VitalsRepositoryImpl
    ): DataVitalsRepository

    @Binds
    @Singleton
    abstract fun bindDomainVitalsRepository(
        adapter: VitalsRepositoryAdapter
    ): DomainVitalsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object VitalsNetworkModule {

    @Provides
    @Singleton
    fun provideVitalsApiService(retrofit: Retrofit): VitalsApiService {
        return retrofit.create(VitalsApiService::class.java)
    }
}


