
package citu.edu.stathis.mobile.core.network

import android.util.Log
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import cit.edu.stathis.mobile.BuildConfig

@Singleton
class AuthInterceptor @Inject constructor(
        private val authTokenManager: AuthTokenManager,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            val token =
                runBlocking {
                    try {
                        if (BuildConfig.BYPASS_AUTH) "debug" else authTokenManager.accessTokenFlow.first()
                    } catch (e: Exception) {
                        Log.e("AuthInterceptor", "Error getting auth token", e)
                        null
                    }
                }

            val newRequestBuilder = originalRequest.newBuilder()

            if (!token.isNullOrBlank()) {
                newRequestBuilder.header("Authorization", "Bearer $token")
                Log.d("AuthInterceptor", "Added Bearer token auth to request: ${originalRequest.url}")
            }

            if (BuildConfig.BYPASS_AUTH) {
                newRequestBuilder.header("X-Bypass-Auth", "true")
            }
            val newRequest = newRequestBuilder.build()

            return chain.proceed(newRequest)
        }
    }
