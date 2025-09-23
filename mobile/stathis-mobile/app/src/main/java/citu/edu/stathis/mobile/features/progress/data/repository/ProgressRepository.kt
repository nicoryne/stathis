package citu.edu.stathis.mobile.features.progress.data.repository

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
     * Get the overall progress for the current student
     */
    suspend fun getStudentProgress(): Flow<StudentProgress>
    
    /**
     * Get detailed progress for a specific classroom
     */
    suspend fun getClassroomProgress(classroomId: String): Flow<ClassroomProgressSummary>
    
    /**
     * Get all achievements for the current student
     */
    suspend fun getAchievements(
        category: String? = null,
        unlocked: Boolean? = null
    ): Flow<List<Achievement>>
    
    /**
     * Get all badges for the current student
     */
    suspend fun getBadges(
        category: String? = null,
        rarity: String? = null,
        unlocked: Boolean? = null
    ): Flow<List<Badge>>
    
    /**
     * Get recent activities for the current student
     */
    suspend fun getRecentActivities(
        limit: Int = 20,
        offset: Int = 0,
        type: String? = null
    ): Flow<List<ProgressActivity>>
    
    /**
     * Get leaderboard for a specific classroom
     */
    suspend fun getClassroomLeaderboard(
        classroomId: String,
        limit: Int = 10
    ): Flow<List<LeaderboardEntry>>
    
    /**
     * Get global leaderboard across all classrooms
     */
    suspend fun getGlobalLeaderboard(
        limit: Int = 10
    ): Flow<List<LeaderboardEntry>>
}
