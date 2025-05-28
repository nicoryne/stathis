package citu.edu.stathis.mobile.features.vitals.data.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.domain.VitalsApiService
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsRequestDto
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
            vitalsApiService.saveVitals(requestDto)
            ClientResponse(success = true, message = "Vitals saved successfully.", data = Unit)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "Failed to save vitals.", data = null)
        }
    }

    override fun getVitalsHistory(userId: String): Flow<ClientResponse<List<VitalSigns>>> = flow {
        try {
            val responseDtoList = vitalsApiService.getVitalsHistory(userId)
            val vitalSignsList = responseDtoList.map { it.toDomain() }
            emit(ClientResponse(success = true, message = "History fetched", data = vitalSignsList))
        } catch (e: Exception) {
            emit(ClientResponse(success = false, message = e.message ?: "Failed to fetch history.", data = null))
        }
    }

    override suspend fun deleteVitalRecord(recordId: String): ClientResponse<Unit> {
        return try {
            vitalsApiService.deleteVitalRecord(recordId)
            ClientResponse(success = true, message = "Record deleted.", data = Unit)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "Failed to delete record.", data = null)
        }
    }
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