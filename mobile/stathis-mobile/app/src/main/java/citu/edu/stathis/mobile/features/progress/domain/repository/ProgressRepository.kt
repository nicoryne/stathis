package citu.edu.stathis.mobile.features.progress.domain.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.progress.data.api.LeaderboardEntry
import citu.edu.stathis.mobile.features.progress.data.model.Achievement
import citu.edu.stathis.mobile.features.progress.data.model.Badge
import citu.edu.stathis.mobile.features.progress.data.model.ClassroomProgressSummary
import citu.edu.stathis.mobile.features.progress.data.model.ProgressActivity
import citu.edu.stathis.mobile.features.progress.data.model.StudentProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for student progress tracking
 */
interface ProgressRepository {
    /**
     * Gets the overall progress for the current student
     */
    suspend fun getStudentProgress(): Flow<ClientResponse<StudentProgress>>
    
    /**
     * Gets the progress for a specific classroom
     */
    suspend fun getClassroomProgress(classroomId: String): Flow<ClientResponse<ClassroomProgressSummary>>
    
    /**
     * Gets achievements filtered by category and unlock status
     */
    suspend fun getAchievements(
        category: String? = null,
        unlocked: Boolean? = null
    ): Flow<ClientResponse<List<Achievement>>>
    
    /**
     * Gets badges filtered by category, rarity, and unlock status
     */
    suspend fun getBadges(
        category: String? = null,
        rarity: String? = null,
        unlocked: Boolean? = null
    ): Flow<ClientResponse<List<Badge>>>
    
    /**
     * Gets recent activities with pagination and filtering
     */
    suspend fun getRecentActivities(
        limit: Int = 10,
        offset: Int = 0,
        type: String? = null
    ): Flow<ClientResponse<List<ProgressActivity>>>
    
    /**
     * Gets the leaderboard for a specific classroom
     */
    suspend fun getClassroomLeaderboard(
        classroomId: String,
        limit: Int = 10
    ): Flow<ClientResponse<List<LeaderboardEntry>>>
    
    /**
     * Gets the global leaderboard
     */
    suspend fun getGlobalLeaderboard(limit: Int = 10): Flow<ClientResponse<List<LeaderboardEntry>>>
}
