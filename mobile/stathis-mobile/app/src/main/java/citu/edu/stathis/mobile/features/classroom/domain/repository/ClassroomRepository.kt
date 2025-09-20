package citu.edu.stathis.mobile.features.classroom.domain.repository

import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.data.model.ClassroomProgress
import citu.edu.stathis.mobile.features.tasks.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for classroom management
 */
interface ClassroomRepository {
    /**
     * Get all classrooms for the current student
     */
    suspend fun getStudentClassrooms(): Flow<List<Classroom>>
    
    /**
     * Get a specific classroom by ID
     */
    suspend fun getStudentClassroom(classroomId: String): Flow<Classroom>
    
    /**
     * Enroll the student in a classroom using a classroom code
     */
    suspend fun enrollInClassroom(classroomCode: String): Flow<Classroom>
    
    /**
     * Get all tasks for a specific classroom
     */
    suspend fun getClassroomTasks(classroomId: String): Flow<List<Task>>
    
    /**
     * Get the student's progress in a specific classroom
     */
    suspend fun getClassroomProgress(classroomId: String): Flow<ClassroomProgress>
    
    /**
     * Leave a classroom (unenroll)
     */
    suspend fun leaveClassroom(classroomId: String): Flow<Boolean>
    
    /**
     * Get active (ongoing) tasks for a specific classroom
     */
    suspend fun getActiveClassroomTasks(classroomId: String): Flow<List<Task>>
    
    /**
     * Get completed tasks for a specific classroom
     */
    suspend fun getCompletedClassroomTasks(classroomId: String): Flow<List<Task>>
}
