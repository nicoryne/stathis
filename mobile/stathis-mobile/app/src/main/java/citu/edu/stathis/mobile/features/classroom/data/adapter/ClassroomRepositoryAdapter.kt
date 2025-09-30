package citu.edu.stathis.mobile.features.classroom.data.adapter

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.data.repository.ClassroomRepository as DataClassroomRepository
import citu.edu.stathis.mobile.features.classroom.domain.repository.ClassroomRepository as DomainClassroomRepository
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Adapter to expose the data-layer ClassroomRepository as the domain-layer ClassroomRepository.
 */
class ClassroomRepositoryAdapter @Inject constructor(
    private val dataRepo: DataClassroomRepository
) : DomainClassroomRepository {

    override suspend fun getStudentClassrooms(): Flow<List<Classroom>> {
        return dataRepo.getStudentClassrooms()
    }

    override suspend fun getStudentClassroom(classroomId: String): Flow<Classroom> {
        return dataRepo.getStudentClassroom(classroomId)
    }

    override suspend fun enrollInClassroom(classroomCode: String): Flow<Classroom> {
        return dataRepo.enrollInClassroom(classroomCode)
    }

    override suspend fun getClassroomTasks(classroomId: String): Flow<List<Task>> {
        return dataRepo.getClassroomTasks(classroomId)
    }

    override suspend fun leaveClassroom(classroomId: String): Flow<Boolean> {
        // If data repo exposes leave API later, delegate; for now return a never-emitting flow or throw
        throw UnsupportedOperationException("leaveClassroom not implemented in data repository")
    }

    override suspend fun getActiveClassroomTasks(classroomId: String): Flow<List<Task>> {
        // Filter using existing getClassroomTasks for now
        return dataRepo.getClassroomTasks(classroomId)
    }

    override suspend fun getCompletedClassroomTasks(classroomId: String): Flow<List<Task>> {
        // Filter using existing getClassroomTasks for now
        return dataRepo.getClassroomTasks(classroomId)
    }
}


