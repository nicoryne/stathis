package citu.edu.stathis.mobile.features.vitals.data.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.domain.VitalsApiService
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsRequestDto
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsResponseDto
import citu.edu.stathis.mobile.features.vitals.domain.repository.VitalsRepository
import citu.edu.stathis.mobile.features.vitals.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class VitalsRepositoryImpl @Inject constructor(
    private val vitalsApiService: VitalsApiService
) : VitalsRepository {

    override suspend fun saveVitals(vitalSigns: VitalSigns): ClientResponse<Unit> {
        return try {
            val requestDto = VitalsRequestDto(
                studentId = vitalSigns.userId,
                heartRate = vitalSigns.heartRate,
                oxygenSaturation = vitalSigns.oxygenSaturation.toInt(),
                timestamp = vitalSigns.timestamp.format(DateTimeFormatter.ISO_DATE_TIME),
                classroomId = vitalSigns.classroomId,
                taskId = vitalSigns.taskId,
                isPreActivity = vitalSigns.isPreActivity,
                isPostActivity = vitalSigns.isPostActivity
            )
            val response = vitalsApiService.saveVitals(requestDto)
            if (response.isSuccessful) {
                ClientResponse(success = true, message = "Vitals saved successfully.", data = Unit)
            } else {
                when (response.code()) {
                    403 -> ClientResponse(
                        success = false,
                        message = "You don't have permission to save vitals. Please check your enrollment status.",
                        data = null
                    )
                    401 -> ClientResponse(
                        success = false,
                        message = "Session expired. Please log in again.",
                        data = null
                    )
                    else -> ClientResponse(
                        success = false,
                        message = "Failed to save vitals: HTTP ${response.code()}",
                        data = null
                    )
                }
            }
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "Failed to save vitals.", data = null)
        }
    }

    override fun getVitalsHistory(userId: String): Flow<ClientResponse<List<VitalSigns>>> = flow {
        try {
            val response = vitalsApiService.getVitalsHistory(userId)
            if (response.isSuccessful) {
                val vitalSignsList = response.body()?.map { it.toDomain() } ?: emptyList()
                emit(ClientResponse(success = true, message = "History fetched", data = vitalSignsList))
            } else {
                when (response.code()) {
                    403 -> emit(ClientResponse(
                        success = false,
                        message = "You don't have permission to view vitals history. Please check your enrollment status.",
                        data = null
                    ))
                    401 -> emit(ClientResponse(
                        success = false,
                        message = "Session expired. Please log in again.",
                        data = null
                    ))
                    else -> emit(ClientResponse(
                        success = false,
                        message = "Failed to fetch history: HTTP ${response.code()}",
                        data = null
                    ))
                }
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                403 -> "You don't have permission to view vitals history. Please check your enrollment status."
                401 -> "Session expired. Please log in again."
                else -> "Failed to fetch history: HTTP ${e.code()}"
            }
            emit(ClientResponse(success = false, message = errorMessage, data = null))
        } catch (e: Exception) {
            emit(ClientResponse(success = false, message = e.message ?: "Failed to fetch history.", data = null))
        }
    }

    override suspend fun deleteVitalRecord(recordId: String): ClientResponse<Unit> {
        return try {
            val response = vitalsApiService.deleteVitalRecord(recordId)
            if (response.isSuccessful) {
                ClientResponse(success = true, message = "Record deleted.", data = Unit)
            } else {
                when (response.code()) {
                    403 -> ClientResponse(
                        success = false,
                        message = "You don't have permission to delete this record.",
                        data = null
                    )
                    401 -> ClientResponse(
                        success = false,
                        message = "Session expired. Please log in again.",
                        data = null
                    )
                    else -> ClientResponse(
                        success = false,
                        message = "Failed to delete record: HTTP ${response.code()}",
                        data = null
                    )
                }
            }
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "Failed to delete record.", data = null)
        }
    }

    // Domain methods - not implemented yet, throw UnsupportedOperationException
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
}

fun VitalsResponseDto.toDomain(): VitalSigns {
    return VitalSigns(
        id = this.physicalId,
        userId = this.studentId,
        systolicBP = this.bpSys ?: 0,
        diastolicBP = this.bpDia ?: 0,
        heartRate = this.heartRate ?: 0,
        respirationRate = this.respirationRate ?: 0,
        temperature = this.temperature ?: 0f,
        oxygenSaturation = this.oxygenSaturation?.toFloat() ?: 0f,
        timestamp = LocalDateTime.parse(this.timestamp, DateTimeFormatter.ISO_DATE_TIME),
        deviceName = "Backend Record",
        classroomId = this.classroomId,
        taskId = this.taskId,
        isPreActivity = this.isPreActivity,
        isPostActivity = this.isPostActivity
    )
}