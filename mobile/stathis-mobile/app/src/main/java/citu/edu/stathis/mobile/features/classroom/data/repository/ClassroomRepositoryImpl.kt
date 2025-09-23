package citu.edu.stathis.mobile.features.classroom.data.repository

import citu.edu.stathis.mobile.features.classroom.data.api.ClassroomService
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.data.model.ClassroomProgress
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ClassroomRepositoryImpl @Inject constructor(
    private val classroomService: ClassroomService
) : ClassroomRepository {

    override suspend fun getStudentClassrooms(): Flow<List<Classroom>> = flow {
        val response = classroomService.getStudentClassrooms()
        if (response.isSuccessful) {
            response.body()?.let { classrooms -> emit(classrooms) }
        }
    }

    override suspend fun getStudentClassroom(classroomId: String): Flow<Classroom> = flow {
        val response = classroomService.getStudentClassroom(classroomId)
        if (response.isSuccessful) {
            response.body()?.let { classroom -> emit(classroom) }
        }
    }

    override suspend fun enrollInClassroom(classroomCode: String): Flow<Classroom> = flow {
        val response = classroomService.enrollInClassroom(classroomCode)
        if (response.isSuccessful) {
            response.body()?.let { classroom -> emit(classroom) }
        }
    }

    override suspend fun getClassroomTasks(classroomId: String): Flow<List<Task>> = flow {
        val response = classroomService.getClassroomTasks(classroomId)
        if (response.isSuccessful) {
            response.body()?.let { tasks -> emit(tasks) }
        }
    }

    override suspend fun getClassroomProgress(classroomId: String): Flow<ClassroomProgress> = flow {
        val response = classroomService.getClassroomProgress(classroomId)
        if (response.isSuccessful) {
            response.body()?.let { progress -> emit(progress) }
        }
    }
} 