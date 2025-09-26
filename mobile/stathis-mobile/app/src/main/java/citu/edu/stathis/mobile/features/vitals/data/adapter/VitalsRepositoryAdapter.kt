package citu.edu.stathis.mobile.features.vitals.data.adapter

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository as DataVitalsRepository
import citu.edu.stathis.mobile.features.vitals.domain.model.HealthRiskAlert
import citu.edu.stathis.mobile.features.vitals.domain.model.VitalsData
import citu.edu.stathis.mobile.features.vitals.domain.model.VitalsSessionSummary
import citu.edu.stathis.mobile.features.vitals.domain.repository.VitalsRepository as DomainVitalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Adapter that exposes the data-layer VitalsRepository as the domain-layer VitalsRepository.
 * Only API-backed operations are delegated for now.
 */
class VitalsRepositoryAdapter @Inject constructor(
    private val dataRepo: DataVitalsRepository
) : DomainVitalsRepository {

    // Domain, device/health-connect related operations are not implemented in data layer yet
    override fun observeVitals(isExerciseSession: Boolean, sessionId: String?): Flow<VitalsData> =
        flow { throw UnsupportedOperationException("observeVitals not implemented") }

    override suspend fun startMonitoring(isExerciseSession: Boolean, sessionId: String?) {
        throw UnsupportedOperationException("startMonitoring not implemented")
    }

    override suspend fun stopMonitoring() {
        throw UnsupportedOperationException("stopMonitoring not implemented")
    }

    override suspend fun saveVitalsData(vitalsData: VitalsData) {
        throw UnsupportedOperationException("saveVitalsData not implemented")
    }

    override suspend fun saveSessionSummary(summary: VitalsSessionSummary) {
        throw UnsupportedOperationException("saveSessionSummary not implemented")
    }

    override suspend fun getSessionSummary(sessionId: String): VitalsSessionSummary? {
        throw UnsupportedOperationException("getSessionSummary not implemented")
    }

    override suspend fun checkHealthRisks(vitalsData: VitalsData): List<HealthRiskAlert> {
        throw UnsupportedOperationException("checkHealthRisks not implemented")
    }

    override suspend fun sendTeacherWebhook(
        vitalsData: VitalsData,
        healthRisks: List<HealthRiskAlert>
    ) {
        throw UnsupportedOperationException("sendTeacherWebhook not implemented")
    }

    override suspend fun isHealthConnectAvailable(): Boolean {
        throw UnsupportedOperationException("isHealthConnectAvailable not implemented")
    }

    override suspend fun requestHealthConnectPermissions() {
        throw UnsupportedOperationException("requestHealthConnectPermissions not implemented")
    }

    override suspend fun hasRequiredPermissions(): Boolean {
        throw UnsupportedOperationException("hasRequiredPermissions not implemented")
    }

    override fun getVitalsHistory(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<VitalsData>> = flow {
        throw UnsupportedOperationException("getVitalsHistory(range) not implemented")
    }

    override fun observeHeartRate(): Flow<Float> =
        flow { throw UnsupportedOperationException("observeHeartRate not implemented") }

    override fun observeOxygenSaturation(): Flow<Float> =
        flow { throw UnsupportedOperationException("observeOxygenSaturation not implemented") }

    override fun observeTemperature(): Flow<Float> =
        flow { throw UnsupportedOperationException("observeTemperature not implemented") }

    // Delegations to data repo (API-backed)
    override suspend fun saveVitals(vitalSigns: VitalSigns): ClientResponse<Unit> =
        dataRepo.saveVitals(vitalSigns)

    override fun getVitalsHistory(userId: String): Flow<ClientResponse<List<VitalSigns>>> =
        dataRepo.getVitalsHistory(userId)

    override suspend fun deleteVitalRecord(recordId: String): ClientResponse<Unit> =
        dataRepo.deleteVitalRecord(recordId)
}


