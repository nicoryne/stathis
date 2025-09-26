package citu.edu.stathis.mobile.features.classroom.data.repository

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import kotlinx.coroutines.flow.Flow

interface ClassroomRepository {
    suspend fun getStudentClassrooms(): Flow<List<Classroom>>
    suspend fun getStudentClassroom(classroomId: String): Flow<Classroom>
    suspend fun enrollInClassroom(classroomCode: String): Flow<Classroom>
    suspend fun getClassroomTasks(classroomId: String): Flow<List<Task>>
} 