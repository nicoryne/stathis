package citu.edu.stathis.mobile.features.vitals.data.mapper

import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsRequestDto
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsResponseDto
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun VitalSigns.toRequestDto(): VitalsRequestDto = VitalsRequestDto(
    studentId = userId,
    classroomId = classroomId,
    taskId = taskId,
    heartRate = heartRate,
    oxygenSaturation = oxygenSaturation.toInt(),
    timestamp = timestamp.toString(),
    isPreActivity = isPreActivity,
    isPostActivity = isPostActivity
)

fun VitalsResponseDto.toDomain(): VitalSigns = VitalSigns(
    id = null,
    userId = physicalId,
    systolicBP = bpSys ?: 0,
    diastolicBP = bpDia ?: 0,
    heartRate = heartRate ?: 0,
    respirationRate = respirationRate ?: 0,
    temperature = temperature ?: 0f,
    oxygenSaturation = (oxygenSaturation ?: 0).toFloat(),
    timestamp = try {
        LocalDateTime.parse(timestamp)
    } catch (_: Exception) {
        // fallback: treat as instant
        OffsetDateTime.parse(timestamp).toLocalDateTime()
    },
    deviceName = null,
    classroomId = classroomId,
    taskId = taskId,
    isPreActivity = isPreActivity,
    isPostActivity = isPostActivity
)


