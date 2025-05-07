package citu.edu.stathis.mobile.features.vitals.di

import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository
import citu.edu.stathis.mobile.features.vitals.domain.repository.IVitalsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VitalsModule {

    @Binds
    @Singleton
    abstract fun bindVitalsRepository(
        vitalsRepository: VitalsRepository
    ): IVitalsRepository
}