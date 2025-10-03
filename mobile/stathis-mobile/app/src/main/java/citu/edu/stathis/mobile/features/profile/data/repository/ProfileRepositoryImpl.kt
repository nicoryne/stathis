package citu.edu.stathis.mobile.features.profile.data.repository

import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import citu.edu.stathis.mobile.features.profile.data.models.UpdateStudentProfileRequest
import citu.edu.stathis.mobile.features.profile.data.models.UpdateUserProfileRequest
import citu.edu.stathis.mobile.features.profile.domain.ProfileApiService
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApiService: ProfileApiService,
    private val authTokenManager: AuthTokenManager
) : ProfileRepository {

    override suspend fun getUserProfile(): ClientResponse<UserResponseDTO> {
        return try {
            val accessToken = authTokenManager.accessTokenFlow.firstOrNull()
            
            // Handle demo account bypass
            if (accessToken == "debug_access") {
                val mockProfile = UserResponseDTO(
                    physicalId = "debug_user",
                    email = "demo@example.com",
                    firstName = "Demo",
                    lastName = "User",
                    birthdate = null,
                    profilePictureUrl = null,
                    school = "Demo University",
                    course = "Computer Science",
                    yearLevel = 3,  // Changed from String "3" to Int 3
                    role = UserRoles.STUDENT,
                    department = null,  // null for students - teacher-only field
                    positionTitle = null,  // null for students - teacher-only field
                    heightInMeters = null,  // Added missing field
                    weightInKg = null,  // Added missing field
                    emailVerified = true  // Added missing field
                )
                // Ensure identity is stored for downstream features (e.g., task progress fallback)
                authTokenManager.updateUserIdentity(
                    physicalId = mockProfile.physicalId,
                    role = mockProfile.role
                )
                return ClientResponse(
                    success = true,
                    data = mockProfile,
                    message = "Demo profile loaded successfully."
                )
            }
            
            val response = profileApiService.getStudentProfile()
            // Store identity so other repositories (tasks, vitals, etc.) can use it immediately
            authTokenManager.updateUserIdentity(
                physicalId = response.physicalId,
                role = response.role
            )
            ClientResponse(
                success = true, data = response,
                message = "Profile successfully fetched."           )
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Failed to fetch profile.")
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error. Could not fetch profile.")
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown error occurred.")
        }
    }

    override suspend fun updateUserProfile(
        firstName: String,
        lastName: String,
        birthdate: String?,
        profilePictureUrl: String?
    ): ClientResponse<UserResponseDTO> {
        return try {
            val request = UpdateUserProfileRequest(
                firstName = firstName,
                lastName = lastName,
                birthdate = birthdate,
                profilePictureUrl = profilePictureUrl
            )
            // val userId = authTokenManager.getUserId()
            //  val response = profileApiService.updateUserProfile(userId, request)
            val response = profileApiService.updateUserProfile(request)
            ClientResponse(success = true, data = response, message = "Profile updated successfully.")
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Failed to update profile.")
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error. Could not update profile.")
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown error occurred.")
        }
    }

    override suspend fun updateStudentProfile(
        school: String?,
        course: String?,
        yearLevel: Int?
    ): ClientResponse<UserResponseDTO> {
        return try {
            val request = UpdateStudentProfileRequest(
                school = school,
                course = course,
                yearLevel = yearLevel
            )
            // val userId = authTokenManager.getUserId()
            // val response = profileApiService.updateStudentProfile(userId, request)
            val response = profileApiService.updateStudentProfile(request)
            ClientResponse(success = true, data = response, message = "Student profile updated.")
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Failed to update student profile.")
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error. Could not update student profile.")
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown error occurred.")
        }
    }
}