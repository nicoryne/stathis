package citu.edu.stathis.mobile.features.vitals.data.datasource

import citu.edu.stathis.mobile.features.vitals.domain.model.VitalsData
import citu.edu.stathis.mobile.features.vitals.domain.model.VitalsSessionSummary
import retrofit2.Response
import retrofit2.http.*

interface VitalsApi {
    @POST("vitals/data")
    suspend fun saveVitalsData(@Body vitalsData: VitalsData): Response<Unit>
    
    @POST("vitals/session/summary")
    suspend fun saveSessionSummary(@Body summary: VitalsSessionSummary): Response<Unit>
    
    @GET("vitals/session/{sessionId}")
    suspend fun getSessionSummary(@Path("sessionId") sessionId: String): Response<VitalsSessionSummary>
    
    @POST("vitals/webhook/teacher")
    suspend fun sendTeacherWebhook(
        @Body data: Map<String, Any>
    ): Response<Unit>
    
    @GET("vitals/history")
    suspend fun getVitalsHistory(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<VitalsData>>
} 