package citu.edu.stathis.mobile.features.vitals.data.model

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Example of how the VitalsRestRequestDto will be serialized to JSON
 * This matches your backend API specification exactly
 */
object VitalsApiExample {
    
    fun createExamplePayload(): String {
        val exampleDto = VitalsRestRequestDto(
            physicalId = "user123",
            studentId = "student456", 
            classroomId = "classroom789",
            taskId = "task101",
            heartRate = 75,
            oxygenSaturation = 98,
            timestamp = LocalDateTime.now(),
            isPreActivity = false,
            isPostActivity = false
        )
        
        // This is what gets sent to your /api/vitals endpoint
        // Note: timestamp will be serialized as ISO-8601 format by the JSON serializer
        val timestampString = exampleDto.timestamp.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        return """
        {
            "physicalId": "${exampleDto.physicalId}",
            "studentId": "${exampleDto.studentId}",
            "classroomId": "${exampleDto.classroomId}",
            "taskId": "${exampleDto.taskId}",
            "heartRate": ${exampleDto.heartRate},
            "oxygenSaturation": ${exampleDto.oxygenSaturation},
            "timestamp": "$timestampString",
            "isPreActivity": ${exampleDto.isPreActivity},
            "isPostActivity": ${exampleDto.isPostActivity}
        }
        """.trimIndent()
    }
}
