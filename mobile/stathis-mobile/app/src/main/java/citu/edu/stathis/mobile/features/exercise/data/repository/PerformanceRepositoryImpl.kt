package citu.edu.stathis.mobile.features.exercise.data.repository

import citu.edu.stathis.mobile.features.exercise.data.datasource.PerformanceApi
import citu.edu.stathis.mobile.features.exercise.domain.model.PerformanceMetrics
import citu.edu.stathis.mobile.features.exercise.domain.model.PerformanceProgress
import citu.edu.stathis.mobile.features.exercise.domain.model.TeacherWebhookData
import citu.edu.stathis.mobile.features.exercise.domain.repository.PerformanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceRepositoryImpl @Inject constructor(
    private val performanceApi: PerformanceApi
) : PerformanceRepository {

    // In-memory cache for webhooks that failed to send
    private val pendingWebhooks = mutableListOf<TeacherWebhookData>()

    override suspend fun savePerformanceMetrics(metrics: PerformanceMetrics) {
        withContext(Dispatchers.IO) {
            performanceApi.savePerformanceMetrics(metrics)
        }
    }
    
    override fun getPerformanceProgress(
        exerciseId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<PerformanceProgress> = flow {
        val progress = performanceApi.getPerformanceProgress(exerciseId, startDate, endDate)
        emit(progress)
    }.flowOn(Dispatchers.IO)
    
    override suspend fun sendTeacherWebhook(webhookData: TeacherWebhookData) {
        withContext(Dispatchers.IO) {
            try {
                performanceApi.sendTeacherWebhook(webhookData)
            } catch (e: Exception) {
                // If sending fails, cache it for retry
                Timber.e(e, "Failed to send teacher webhook, caching for retry")
                pendingWebhooks.add(webhookData)
            }
        }
    }
    
    // Simple in-memory implementation to replace missing API methods
    override suspend fun cacheWebhookData(webhookData: TeacherWebhookData) {
        pendingWebhooks.add(webhookData)
    }
    
    override suspend fun retryCachedWebhooks() {
        withContext(Dispatchers.IO) {
            if (pendingWebhooks.isEmpty()) return@withContext
            
            val iterator = pendingWebhooks.iterator()
            while (iterator.hasNext()) {
                val webhook = iterator.next()
                try {
                    performanceApi.sendTeacherWebhook(webhook)
                    iterator.remove()
                    Timber.d("Successfully sent cached webhook")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to send cached webhook, will retry later")
                    // Failed again, will retry next time
                }
            }
        }
    }
    
    override suspend fun getAverageAccuracy(exerciseId: String, lastNSessions: Int): Float {
        return withContext(Dispatchers.IO) {
            performanceApi.getAverageAccuracy(exerciseId, lastNSessions)
        }
    }
    
    override suspend fun checkLowAccuracyStreak(exerciseId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val averageAccuracy = getAverageAccuracy(exerciseId, 3)
            averageAccuracy < 70.0f
        }
    }
    
    override fun observeRealtimePerformance(sessionId: String): Flow<PerformanceMetrics> = flow {
        // Simulated data for now
        val metrics = PerformanceMetrics(
            userId = "user1",
            exerciseId = "exercise1",
            accuracy = 85.0f,
            repetitionCount = 5,
            sessionDurationMs = 60000,
            postureIssues = listOf("Shoulders too high", "Back not straight")
        )
        emit(metrics)
    }.flowOn(Dispatchers.IO)
}