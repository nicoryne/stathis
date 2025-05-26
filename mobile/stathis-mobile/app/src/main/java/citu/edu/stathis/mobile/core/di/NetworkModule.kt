package citu.edu.stathis.mobile.core.di

import citu.edu.stathis.mobile.core.network.AuthInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import citu.edu.stathis.mobile.core.network.Constants
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    @Provides
    @Singleton
    fun provideBackendOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideBackendRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(Constants.BACKEND_URL_DEV)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
}
