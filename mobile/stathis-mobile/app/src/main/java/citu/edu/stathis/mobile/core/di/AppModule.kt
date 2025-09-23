package citu.edu.stathis.mobile.core.di

import android.content.Context
import citu.edu.stathis.mobile.core.auth.BiometricHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(
        @ApplicationContext context: Context,
    ): Context = context

    @Provides
    @Singleton
    fun provideBiometricHelper(
        @ApplicationContext context: Context,
    ): BiometricHelper = BiometricHelper(context)
}
