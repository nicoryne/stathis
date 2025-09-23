package citu.edu.stathis.mobile.features.vitals.di

import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import citu.edu.stathis.mobile.features.vitals.domain.usecase.MonitorRealTimeVitalsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Provides ViewModelScoped dependencies for the Vitals feature
 */
@Module
@InstallIn(ViewModelComponent::class)
class VitalsViewModelModule {
    
    @Provides
    @ViewModelScoped
    fun provideMonitorRealTimeVitalsUseCase(
        healthConnectManager: HealthConnectManager
    ): MonitorRealTimeVitalsUseCase {
        return MonitorRealTimeVitalsUseCase(healthConnectManager)
    }
}
