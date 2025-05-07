package citu.edu.stathis.mobile.features.profile.domain.repository

import android.net.Uri
import citu.edu.stathis.mobile.features.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface IUserProfileRepository {
    suspend fun getUserProfile(): Flow<UserProfile?>
    suspend fun updateUserProfile(
        firstName: String,
        lastName: String,
        schoolAttending: String?,
        yearLevel: Short?,
        courseEnrolled: String?
    ): Result<UserProfile>

    suspend fun uploadProfilePicture(imageUri: Uri): Result<String>
    suspend fun deleteProfilePicture(): Result<Unit>
    suspend fun deleteOldProfilePicture(pictureUrl: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}