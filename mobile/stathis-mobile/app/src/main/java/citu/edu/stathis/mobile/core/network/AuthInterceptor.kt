
package citu.edu.stathis.mobile.core.network

import android.util.Log
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import kotlinx.coroutines.flow.firstOrNull
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
            val url = originalRequest.url
            val path = url.encodedPath

            // Do not attach Authorization for public auth endpoints
            val isPublicAuthEndpoint = path.startsWith("/api/auth/") && (
                path.contains("/login") ||
                path.contains("/register") ||
                path.contains("/refresh")
            )

            val token: String? = if (!isPublicAuthEndpoint) {
                runBlocking {
                    try { authTokenManager.accessTokenFlow.firstOrNull() } catch (e: Exception) {
                        Log.e("AuthInterceptor", "Error getting auth token", e); null
                    }
                }
            } else null

            // Also fetch user role to add Spring-friendly headers when available
            val userRole: String? = runBlocking {
                try { authTokenManager.userRoleFlow.firstOrNull()?.name } catch (e: Exception) { null }
            }

            val newRequestBuilder = originalRequest.newBuilder()

            if (!token.isNullOrBlank()) {
                newRequestBuilder.header("Authorization", "Bearer $token")
                Log.d("AuthInterceptor", "Added Bearer token auth to request: ${originalRequest.url}")
            }

            // Add role headers for student-protected APIs (many Spring endpoints check role headers in addition to JWT)
            if (!userRole.isNullOrBlank()) {
                val withoutPrefix = userRole.removePrefix("ROLE_")
                newRequestBuilder.header("X-User-Role", withoutPrefix)
                newRequestBuilder.header("X-User-Role-With-Prefix", "ROLE_${withoutPrefix}")
                newRequestBuilder.header("X-Spring-Security-Role", "ROLE_${withoutPrefix}")
            }

            val newRequest = newRequestBuilder.build()

            return chain.proceed(newRequest)
        }
    }
