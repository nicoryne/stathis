package citu.edu.stathis.mobile.features.progress.data.api

import citu.edu.stathis.mobile.features.progress.data.model.Achievement
import citu.edu.stathis.mobile.features.progress.data.model.Badge
import citu.edu.stathis.mobile.features.progress.data.model.ClassroomProgressSummary
import citu.edu.stathis.mobile.features.progress.data.model.ProgressActivity
import citu.edu.stathis.mobile.features.progress.data.model.StudentProgress
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API service for student progress tracking
 */
interface ProgressService {
    /**
     * Get the overall progress for the current student
     */
    @GET("api/student/progress")
    suspend fun getStudentProgress(): Response<StudentProgress>
    
    /**
     * Get detailed progress for a specific classroom
     */
    @GET("api/student/progress/classroom/{classroomId}")
    suspend fun getClassroomProgress(
        @Path("classroomId") classroomId: String
    ): Response<ClassroomProgressSummary>
    
    /**
     * Get all achievements for the current student
     */
    @GET("api/student/progress/achievements")
    suspend fun getAchievements(
        @Query("category") category: String? = null,
        @Query("unlocked") unlocked: Boolean? = null
    ): Response<List<Achievement>>
    
    /**
     * Get all badges for the current student
     */
    @GET("api/student/progress/badges")
    suspend fun getBadges(
        @Query("category") category: String? = null,
        @Query("rarity") rarity: String? = null,
        @Query("unlocked") unlocked: Boolean? = null
    ): Response<List<Badge>>
    
    /**
     * Get recent activities for the current student
     */
    @GET("api/student/progress/activities")
    suspend fun getRecentActivities(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("type") type: String? = null
    ): Response<List<ProgressActivity>>
    
    /**
     * Get leaderboard for a specific classroom
     */
    @GET("api/student/progress/leaderboard/classroom/{classroomId}")
    suspend fun getClassroomLeaderboard(
        @Path("classroomId") classroomId: String,
        @Query("limit") limit: Int = 10
    ): Response<List<LeaderboardEntry>>
    
    /**
     * Get global leaderboard across all classrooms
     */
    @GET("api/student/progress/leaderboard/global")
    suspend fun getGlobalLeaderboard(
        @Query("limit") limit: Int = 10
    ): Response<List<LeaderboardEntry>>
}

/**
 * Represents an entry in a leaderboard
 */
data class LeaderboardEntry(
    val userId: String,
    val fullName: String,
    val rank: Int,
    val points: Int,
    val isCurrentUser: Boolean
)
