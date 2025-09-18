package citu.edu.stathis.mobile.features.vitals.domain.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface VitalsRepository {
    fun observeVitals(isExerciseSession: Boolean = false, sessionId: String? = null): Flow<VitalsData>
    
    suspend fun startMonitoring(isExerciseSession: Boolean = false, sessionId: String? = null)
    
    suspend fun stopMonitoring()
    
    suspend fun saveVitalsData(vitalsData: VitalsData)
    
    suspend fun saveSessionSummary(summary: VitalsSessionSummary)
    
    suspend fun getSessionSummary(sessionId: String): VitalsSessionSummary?
    
    suspend fun checkHealthRisks(vitalsData: VitalsData): List<HealthRiskAlert>
    
    suspend fun sendTeacherWebhook(
        vitalsData: VitalsData,
        healthRisks: List<HealthRiskAlert>
    )
    
    suspend fun isHealthConnectAvailable(): Boolean
    
    suspend fun requestHealthConnectPermissions()
    
    suspend fun hasRequiredPermissions(): Boolean
    
    fun getVitalsHistory(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<VitalsData>>
    
    // Additional methods needed for DashboardViewModel
    fun observeHeartRate(): Flow<Float>
    
    fun observeOxygenSaturation(): Flow<Float>
    
    fun observeTemperature(): Flow<Float>
    
    // Methods from implementation
    suspend fun saveVitals(vitalSigns: VitalSigns): ClientResponse<Unit>
    
    fun getVitalsHistory(userId: String): Flow<ClientResponse<List<VitalSigns>>>
    
    suspend fun deleteVitalRecord(recordId: String): ClientResponse<Unit>
} 