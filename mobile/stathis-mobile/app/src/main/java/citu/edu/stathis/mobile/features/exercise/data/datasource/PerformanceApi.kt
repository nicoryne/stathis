package citu.edu.stathis.mobile.features.exercise.data.datasource

import citu.edu.stathis.mobile.features.exercise.domain.model.PerformanceMetrics
import citu.edu.stathis.mobile.features.exercise.domain.model.PerformanceProgress
import citu.edu.stathis.mobile.features.exercise.domain.model.TeacherWebhookData
import retrofit2.http.*
import java.time.LocalDateTime

interface PerformanceApi {
    @POST("performance/metrics")
    suspend fun savePerformanceMetrics(@Body metrics: PerformanceMetrics)

    @GET("performance/progress/{exerciseId}")
    suspend fun getPerformanceProgress(
        @Path("exerciseId") exerciseId: String,
        @Query("startDate") startDate: LocalDateTime,
        @Query("endDate") endDate: LocalDateTime
    ): PerformanceProgress

    @POST("performance/webhook/teacher")
    suspend fun sendTeacherWebhook(@Body webhookData: TeacherWebhookData)

    @GET("performance/accuracy/{exerciseId}")
    suspend fun getAverageAccuracy(
        @Path("exerciseId") exerciseId: String,
        @Query("lastNSessions") lastNSessions: Int
    ): Float

    @GET("performance/realtime/{sessionId}")
    suspend fun getRealtimePerformance(@Path("sessionId") sessionId: String): PerformanceMetrics
} 