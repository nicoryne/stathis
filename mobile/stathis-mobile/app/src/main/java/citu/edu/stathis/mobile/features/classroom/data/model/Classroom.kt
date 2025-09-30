package citu.edu.stathis.mobile.features.classroom.data.model

data class Classroom(
    val physicalId: String,
    val name: String,
    val description: String,
    val classroomCode: String,
    val teacherId: String,
    val teacherName: String,
    val isActive: Boolean,
    val studentCount: Int,
    val createdAt: String,
    val updatedAt: String
) 