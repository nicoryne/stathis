package citu.edu.stathis.mobile.core.di

import android.app.Application
import android.content.Context
import citu.edu.stathis.mobile.core.theme.ThemePreferences
import citu.edu.stathis.mobile.core.theme.ThemeViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for theme-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences {
        return ThemePreferences(context)
    }
}


