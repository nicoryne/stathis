package citu.edu.stathis.mobile.core.network

import android.util.Log
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator that refreshes access tokens on 401 responses and retries once.
 * - Serializes refresh attempts by synchronizing on an internal lock.
 * - Uses the latest refresh token from AuthTokenManager.
 * - Clears auth data on refresh failure to force logout upstream.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authTokenManager: AuthTokenManager,
    private val gson: Gson
) : Authenticator {

    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loops: if we've already attempted with an Authorization header once, don't loop.
        if (responseCount(response) >= 2) {
            return null
        }

        synchronized(refreshLock) {
            // Another thread may have refreshed while we waited; check if access token changed.
            val currentAccess: String? = runBlocking { authTokenManager.accessTokenFlow.firstOrNull() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (!currentAccess.isNullOrBlank() && currentAccess != requestToken) {
                // Build a new request with the latest token
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccess")
                    .build()
            }

            // Perform refresh using current refresh token
            val refreshToken: String? = runBlocking { authTokenManager.refreshTokenFlow.firstOrNull() }
            if (refreshToken.isNullOrBlank()) {
                Log.w("TokenAuthenticator", "No refresh token; cannot refresh.")
                // Clear any stale auth
                runBlocking { authTokenManager.clearAuthData() }
                return null
            }

            // Perform refresh using a dedicated bare OkHttp client (no authenticator to avoid recursion)
            return try {
                val refreshResponse = performRefresh(refreshToken)
                if (refreshResponse != null) {
                    val newAccess = refreshResponse.accessToken
                    val newRefresh = refreshResponse.refreshToken
                    // Persist new tokens quickly
                    runBlocking { authTokenManager.updateTokens(newAccess, newRefresh) }
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .build()
                } else {
                    runBlocking { authTokenManager.clearAuthData() }
                    null
                }
            } catch (e: Exception) {
                Log.w("TokenAuthenticator", "Refresh failed: ${e.message}")
                runBlocking { authTokenManager.clearAuthData() }
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}

private data class RefreshDto(val accessToken: String, val refreshToken: String)

private fun performRefresh(refreshToken: String): RefreshDto? {
    val url = cit.edu.stathis.mobile.BuildConfig.API_BASE_URL.trimEnd('/') + "/api/auth/refresh?refreshToken=" + refreshToken
    val client = okhttp3.OkHttpClient.Builder().build()
    val request = okhttp3.Request.Builder()
        .url(url)
        .post(okhttp3.RequestBody.create(null, ByteArray(0)))
        .build()
    client.newCall(request).execute().use { resp ->
        if (!resp.isSuccessful) return null
        val body = resp.body?.string() ?: return null
        // Basic parse for tokens
        val json = com.google.gson.JsonParser.parseString(body).asJsonObject
        val access = json.get("accessToken")?.asString ?: return null
        val refresh = json.get("refreshToken")?.asString ?: refreshToken
        return RefreshDto(access, refresh)
    }
}


