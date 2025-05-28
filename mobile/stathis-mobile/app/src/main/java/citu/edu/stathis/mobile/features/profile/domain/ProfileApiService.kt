package citu.edu.stathis.mobile.features.profile.domain

import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import citu.edu.stathis.mobile.features.profile.data.models.UpdateStudentProfileRequest
import citu.edu.stathis.mobile.features.profile.data.models.UpdateUserProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileApiService {

    @GET("api/users/profile/student")
    suspend fun getStudentProfile(): UserResponseDTO

    @PUT("api/users/profile")
    suspend fun updateUserProfile(@Body request: UpdateUserProfileRequest): UserResponseDTO

    @PUT("api/users/profile/student")
    suspend fun updateStudentProfile(@Body request: UpdateStudentProfileRequest): UserResponseDTO
}