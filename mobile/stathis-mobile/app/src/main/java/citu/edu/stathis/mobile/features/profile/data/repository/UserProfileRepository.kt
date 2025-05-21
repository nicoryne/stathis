package citu.edu.stathis.mobile.features.profile.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.features.profile.domain.model.UserProfile
import citu.edu.stathis.mobile.features.profile.domain.repository.IUserProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : IUserProfileRepository {

    @Serializable
    data class UserProfileDto(
        val email: String,
        val created_at: String,
        val updated_at: String,
        val first_name: String,
        val last_name: String,
        val picture_url: String?,
        val user_role: String,
        val school_attending: String?,
        val year_level: Short,
        val course_enrolled: String?
    )

    override suspend fun getUserProfile(): Flow<UserProfile?> = flow {
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: run {
                return@flow
            }
            val response = supabaseClient.postgrest["user_profile"]
                .select {
                   filter {
                       eq("user_id", userId)
                   }
                }
                .decodeSingle<UserProfileDto>()


            emit(response.toUserProfile())
        } catch (_: Exception) {
        }
    }

    override suspend fun updateUserProfile(
        firstName: String,
        lastName: String,
        schoolAttending: String?,
        yearLevel: Short?,
        courseEnrolled: String?
    ): Result<UserProfile> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return Result.failure(
                IllegalStateException("User not authenticated")
            )

            Log.d("UserProfileRepository", "Updating profile..")
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

            supabaseClient.postgrest["user_profile"]
                .update({
                    set("first_name", firstName)
                    set("last_name", lastName)
                    set("updated_at", now)
                    set("school_attending", schoolAttending)
                    set("year_level", yearLevel)
                    set("course_enrolled", courseEnrolled)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }

            val response = supabaseClient.postgrest["user_profile"]
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserProfileDto>()

            return Result.success(response.toUserProfile())
        } catch (e: Exception) {
            Log.e("UserProfileRepository", "${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun uploadProfilePicture(imageUri: Uri): Result<String> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return Result.failure(
                IllegalStateException("User not authenticated")
            )

            // Create a temporary file from the URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(IllegalStateException("Failed to open image"))

            val fileExtension = getFileExtension(context, imageUri)
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("profile_", ".$fileExtension", context.cacheDir)
            }

            withContext(Dispatchers.IO) {
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()
            }

            // Update the profile with the new picture URL
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

            // Upload to Supabase Storage
            val fileName = "$now.$fileExtension"
            val path = "$userId/$fileName"

            // Upload the file
            supabaseClient.storage["user-avatars"].upload(
                path = path,
                file = tempFile,
            )

            // Get the public URL
            val publicUrl = supabaseClient.storage["user-avatars"].publicUrl(path)

            supabaseClient.postgrest["user_profile"]
                .update({
                    set("picture_url", publicUrl)
                    set("updated_at", now)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }

            // Delete the temp file
            tempFile.delete()

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfilePicture(): Result<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return Result.failure(
                IllegalStateException("User not authenticated")
            )

            // Get current profile to find the file name
            val profile = supabaseClient.postgrest["user_profile"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserProfileDto>()

            // If there's a picture URL, extract the file path and delete it
            if (!profile.picture_url.isNullOrEmpty()) {
                val path = profile.picture_url.substringAfterLast("user-avatars/")

                // Delete the file from storage
                supabaseClient.storage["user-avatars"].delete(path)

                // Update the profile to remove the picture URL
                val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

                supabaseClient.postgrest["user_profile"]
                    .update({
                        set("picture_url", "")
                        set("updated_at", now)
                    }) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteOldProfilePicture(pictureUrl: String): Result<Unit> {
        return try {
            val path = pictureUrl.substringAfterLast("user-avatars/")

            supabaseClient.storage["user-avatars"].delete(path)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            authRepository.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun UserProfileDto.toUserProfile(): UserProfile {
        val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
        return UserProfile(
            createdAt = LocalDateTime.parse(created_at, dateFormatter),
            updatedAt = LocalDateTime.parse(updated_at, dateFormatter),
            firstName = first_name,
            lastName = last_name,
            pictureUrl = picture_url,
            userRole = user_role,
            schoolAttending = school_attending,
            yearLevel = year_level,
            courseEnrolled = course_enrolled,
            email = email
        )
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: return "jpg"
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
    }
}

data class PictureUrlWrapper(val picture_url: String)


