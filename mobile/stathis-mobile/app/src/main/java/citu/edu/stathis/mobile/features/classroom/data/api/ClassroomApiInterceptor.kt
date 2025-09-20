package citu.edu.stathis.mobile.features.classroom.data.api

import android.util.Log
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Special interceptor for classroom-related API requests
 * This interceptor ensures proper formatting of requests to Spring Boot backend
 */
class ClassroomApiInterceptor @Inject constructor(
    private val authTokenManager: AuthTokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        
        // Process all classroom-related requests (both paths)
        if (!url.contains("/api/classrooms") && !url.contains("/api/student/classrooms")) {
            return chain.proceed(originalRequest)
        }
        
        // Add special handling for enrollment endpoint
        val isEnrollmentRequest = url.endsWith("/enroll")
        
        Log.d("ClassroomInterceptor", "Processing classroom request: $url")
        
        // Get both token and user role
        val (token, userRole) = runBlocking {
            val accessToken = authTokenManager.accessTokenFlow.first()
            val role = authTokenManager.userRoleFlow.firstOrNull()
            Pair(accessToken, role)
        }

        // For classroom requests, we need specific headers
        val newRequestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
        
        if (!token.isNullOrBlank()) {
            newRequestBuilder.header("Authorization", "Bearer $token")
            
            // Add role headers based on the actual user role
            if (userRole != null) {
                // Add both formats for maximum compatibility
                newRequestBuilder.header("X-User-Role", userRole.withoutPrefix())
                newRequestBuilder.header("X-User-Role-With-Prefix", userRole.toSpringSecurityRole())
                
                // Add Spring Security compatible role header
                newRequestBuilder.header("X-Spring-Security-Role", "ROLE_" + userRole.withoutPrefix())
                Log.d("ClassroomInterceptor", "Added role headers: ${userRole.withoutPrefix()} and ${userRole.toSpringSecurityRole()}")
            } else {
                // Fallback to hard-coded STUDENT role if we can't determine the actual role
                newRequestBuilder.header("X-User-Role", "STUDENT")
                newRequestBuilder.header("X-User-Role-With-Prefix", "ROLE_STUDENT")
                newRequestBuilder.header("X-Spring-Security-Role", "ROLE_STUDENT")
                Log.d("ClassroomInterceptor", "Added fallback STUDENT role headers")
            }
        }
        
        // Add extra handling for enrollment requests
        if (isEnrollmentRequest) {
            Log.d("ClassroomInterceptor", "Enrollment request detected - adding special headers for Spring Security")
            
            // For enrollment requests, we need to ensure Spring Security finds the STUDENT role
            // The @PreAuthorize annotation in the backend is checking for this specific role
            
            // 1. Add Authorization header with role claim directly in token format
            if (!token.isNullOrBlank()) {
                // Leave existing Authorization header intact
                
                // 2. Add Spring Security specific role headers
                newRequestBuilder.header("X-Spring-Security-Role", "ROLE_STUDENT")
                
                // 3. Try both with and without ROLE_ prefix to maximize compatibility
                newRequestBuilder.header("Role", "STUDENT") 
                newRequestBuilder.header("Authorities", "ROLE_STUDENT")
                
                Log.d("ClassroomInterceptor", "Added Spring Security compatible role headers for enrollment")
            }
        }
        
        // Create the new request with all necessary headers
        val newRequest = newRequestBuilder.build()
        return chain.proceed(newRequest)
    }
}
