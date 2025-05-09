package citu.edu.stathis.mobile.core.di

import android.content.Context
import android.util.Log
import cit.edu.stathis.mobile.BuildConfig
import citu.edu.stathis.mobile.core.auth.BiometricHelper
import citu.edu.stathis.mobile.core.data.PreferencesManager
import citu.edu.stathis.mobile.core.network.SupabaseInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideSupabase(): SupabaseClient {
        return SupabaseInstance.instance
    }

    @Provides
    @Singleton
    fun provideBiometricHelper(@ApplicationContext context: Context): BiometricHelper {
        return BiometricHelper(context)
    }
}
