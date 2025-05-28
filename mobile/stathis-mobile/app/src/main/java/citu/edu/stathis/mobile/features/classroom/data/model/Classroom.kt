package citu.edu.stathis.mobile.features.classroom.data.model

import java.time.OffsetDateTime

data class Classroom(
    val physicalId: String,
    val name: String,
    val description: String,
    val classroomCode: String,
    val teacherId: String,
    val teacherName: String,
    val isActive: Boolean,
    val studentCount: Int,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) 