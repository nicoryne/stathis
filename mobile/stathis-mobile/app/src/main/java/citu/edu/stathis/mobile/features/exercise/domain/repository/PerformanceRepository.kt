package citu.edu.stathis.mobile.features.exercise.domain.repository

import citu.edu.stathis.mobile.features.exercise.domain.model.PerformanceMetrics
import citu.edu.stathis.mobile.features.exercise.domain.model.PerformanceProgress
import citu.edu.stathis.mobile.features.exercise.domain.model.TeacherWebhookData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface PerformanceRepository {
    suspend fun savePerformanceMetrics(metrics: PerformanceMetrics)
    
    fun getPerformanceProgress(
        exerciseId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<PerformanceProgress>
    
    suspend fun sendTeacherWebhook(webhookData: TeacherWebhookData)
    
    suspend fun cacheWebhookData(webhookData: TeacherWebhookData)
    
    suspend fun retryCachedWebhooks()
    
    suspend fun getAverageAccuracy(exerciseId: String, lastNSessions: Int): Float
    
    suspend fun checkLowAccuracyStreak(exerciseId: String): Boolean // Returns true if last 3 sessions < 70%
    
    fun observeRealtimePerformance(sessionId: String): Flow<PerformanceMetrics>
} 