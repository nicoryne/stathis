package citu.edu.stathis.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class StathisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        // Always plant debug tree for now to avoid BuildConfig issues
        Timber.plant(Timber.DebugTree())
    }
}