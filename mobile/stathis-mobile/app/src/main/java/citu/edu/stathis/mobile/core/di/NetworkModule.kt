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
import cit.edu.stathis.mobile.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideBackendOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: citu.edu.stathis.mobile.core.network.TokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideBackendRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        run {
            val raw = if (BuildConfig.API_BASE_URL.isNotBlank()) BuildConfig.API_BASE_URL else Constants.BACKEND_URL_PROD
            val normalized = raw.trim().let { url -> if (url.endsWith("/")) url else "$url/" }
            Retrofit
                .Builder()
                .baseUrl(normalized)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
}
