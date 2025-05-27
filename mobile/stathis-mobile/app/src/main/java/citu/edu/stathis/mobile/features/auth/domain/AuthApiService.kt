package citu.edu.stathis.mobile.features.auth.domain

import citu.edu.stathis.mobile.features.auth.data.models.LoginRequest
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.data.models.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): Unit

    @POST("api/auth/logout")
    suspend fun logout(@Query("refreshToken") refreshToken: String): Unit

    @POST("api/auth/refresh")
    suspend fun refresh(@Query("refreshToken") refreshToken: String): Unit

    @GET("api/auth/resend-verification-email")
    suspend fun resendVerificationEmail(
        @Query("email") email: String
    ): Unit
}