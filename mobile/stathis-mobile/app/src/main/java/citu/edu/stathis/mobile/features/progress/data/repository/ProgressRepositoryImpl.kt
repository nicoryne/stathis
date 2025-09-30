package citu.edu.stathis.mobile.features.progress.data.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.progress.data.api.LeaderboardEntry
import citu.edu.stathis.mobile.features.progress.data.api.ProgressService
import citu.edu.stathis.mobile.features.progress.data.model.Achievement
import citu.edu.stathis.mobile.features.progress.data.model.Badge
import citu.edu.stathis.mobile.features.progress.data.model.ClassroomProgressSummary
import citu.edu.stathis.mobile.features.progress.data.model.ProgressActivity
import citu.edu.stathis.mobile.features.progress.data.model.StudentProgress
import citu.edu.stathis.mobile.features.progress.domain.repository.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val progressService: ProgressService
) : ProgressRepository {

    override suspend fun getStudentProgress(): Flow<ClientResponse<StudentProgress>> = flow {
        try {
            val response = progressService.getStudentProgress()
            if (response.isSuccessful) {
                val data = response.body()
                emit(ClientResponse(success = true, data = data, message = "Student progress retrieved successfully"))
            } else {
                emit(ClientResponse<StudentProgress>(success = false, data = null, message = "Failed to retrieve student progress: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve student progress: ${e.message}")
            emit(ClientResponse<StudentProgress>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getClassroomProgress(classroomId: String): Flow<ClientResponse<ClassroomProgressSummary>> = flow {
        try {
            val response = progressService.getClassroomProgress(classroomId)
            if (response.isSuccessful) {
                val data = response.body()
                emit(ClientResponse(success = true, data = data, message = "Classroom progress retrieved successfully"))
            } else {
                emit(ClientResponse<ClassroomProgressSummary>(success = false, data = null, message = "Failed to retrieve classroom progress: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve classroom progress: ${e.message}")
            emit(ClientResponse<ClassroomProgressSummary>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun getAchievements(
        category: String?, 
        unlocked: Boolean?
    ): Flow<ClientResponse<List<Achievement>>> = flow {
        // Not supported by backend; map badges-by-student into a simplified Achievement list if needed.
        emit(ClientResponse<List<Achievement>>(success = false, data = emptyList<Achievement>(), message = "Achievements endpoint not available; use badges."))
    }.flowOn(Dispatchers.IO)

    override suspend fun getBadges(
        category: String?, 
        rarity: String?, 
        unlocked: Boolean?
    ): Flow<ClientResponse<List<Badge>>> = flow {
        try {
            // We need studentId; the ProgressService endpoint expects studentId; caller provides it indirectly.
            emit(ClientResponse<List<Badge>>(success = false, data = emptyList<Badge>(), message = "Use getBadgesByStudent via service directly in ViewModel."))
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve badges: ${e.message}")
            emit(ClientResponse<List<Badge>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun getRecentActivities(
        limit: Int, 
        offset: Int, 
        type: String?
    ): Flow<ClientResponse<List<ProgressActivity>>> = flow {
        try {
            val response = progressService.getRecentActivities(limit, offset, type)
            if (response.isSuccessful) {
                val data = response.body()
                emit(ClientResponse(success = true, data = data, message = "Recent activities retrieved successfully"))
            } else {
                emit(ClientResponse<List<ProgressActivity>>(success = false, data = null, message = "Failed to retrieve recent activities: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve recent activities: ${e.message}")
            emit(ClientResponse<List<ProgressActivity>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun getClassroomLeaderboard(
        classroomId: String, 
        limit: Int
    ): Flow<ClientResponse<List<LeaderboardEntry>>> = flow {
        try {
            val response = progressService.getClassroomLeaderboard(classroomId, limit)
            if (response.isSuccessful) {
                val data = response.body()
                emit(ClientResponse(success = true, data = data, message = "Classroom leaderboard retrieved successfully"))
            } else {
                emit(ClientResponse<List<LeaderboardEntry>>(success = false, data = null, message = "Failed to retrieve classroom leaderboard: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve classroom leaderboard: ${e.message}")
            emit(ClientResponse<List<LeaderboardEntry>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun getGlobalLeaderboard(limit: Int): Flow<ClientResponse<List<LeaderboardEntry>>> = flow {
        try {
            val response = progressService.getGlobalLeaderboard(limit)
            if (response.isSuccessful) {
                val data = response.body()
                emit(ClientResponse(success = true, data = data, message = "Global leaderboard retrieved successfully"))
            } else {
                emit(ClientResponse<List<LeaderboardEntry>>(success = false, data = null, message = "Failed to retrieve global leaderboard: ${response.code()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve global leaderboard: ${e.message}")
            emit(ClientResponse<List<LeaderboardEntry>>(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}"))
        }
    }.flowOn(Dispatchers.IO)
}
