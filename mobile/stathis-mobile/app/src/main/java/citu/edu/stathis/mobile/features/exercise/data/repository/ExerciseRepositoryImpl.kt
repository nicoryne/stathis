package citu.edu.stathis.mobile.features.exercise.data.repository

import android.content.Context
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.ExerciseSessionResult
import citu.edu.stathis.mobile.features.exercise.data.ExerciseType
import citu.edu.stathis.mobile.features.exercise.data.datasource.ExerciseApi
import citu.edu.stathis.mobile.features.exercise.data.model.*
import citu.edu.stathis.mobile.features.exercise.domain.ExerciseApiService
import citu.edu.stathis.mobile.features.exercise.domain.model.PostureAnalysis
import citu.edu.stathis.mobile.features.exercise.domain.repository.ExerciseRepository
import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetCurrentUserIdUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localApi: ExerciseApi,
    private val apiService: ExerciseApiService,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ExerciseRepository {

    // ML Kit configuration
    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        // Use the available methods from PoseDetectorOptions
        .build()

    private val poseDetector = PoseDetection.getClient(options)

    // Real-time analysis methods
    override fun analyzePostureInRealTime(exerciseId: String): Flow<PostureAnalysis> = callbackFlow<PostureAnalysis> {
        // Implementation will process camera frames and use ML Kit
        // This is a placeholder for the actual implementation
        awaitClose { poseDetector.close() }
    }.flowOn(Dispatchers.Default)

    override suspend fun saveAnalysisResult(exerciseId: String, analysis: PostureAnalysis) {
        localApi.saveAnalysisResult(exerciseId, analysis)
    }

    override suspend fun startExerciseSession(exerciseId: String) {
        localApi.startExerciseSession(exerciseId)
    }

    override suspend fun endExerciseSession(exerciseId: String) {
        localApi.endExerciseSession(exerciseId)
    }
    
    // API-based methods
    override suspend fun getAvailableExercises(): ClientResponse<List<Exercise>> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserIdUseCase()
            if (userId == null) {
                return@withContext getStaticExercises()
            }

            val response = apiService.getAvailableExercises(userId)
            if (response.isSuccessful) {
                val exercises = response.body()?.map { it.toDomain() } ?: emptyList()
                if (exercises.isEmpty()) {
                    return@withContext getStaticExercises()
                }
                ClientResponse(success = true, data = exercises, message = "Exercises retrieved successfully")
            } else {
                when (response.code()) {
                    403 -> {
                        // For 403, we'll still try to get the exercises without enrollment context
                        val fallbackResponse = apiService.getAvailableExercisesNoEnrollment()
                        if (fallbackResponse.isSuccessful) {
                            val exercises = fallbackResponse.body()?.map { it.toDomain() } ?: emptyList()
                            if (exercises.isEmpty()) {
                                return@withContext getStaticExercises()
                            }
                            ClientResponse(success = true, data = exercises, message = "Showing available exercises (not enrolled)")
                        } else {
                            getStaticExercises()
                        }
                    }
                    401 -> getStaticExercises()
                    else -> getStaticExercises()
                }
            }
        } catch (e: Exception) {
            getStaticExercises()
        }
    }

    private fun getStaticExercises(): ClientResponse<List<Exercise>> {
        val exercises = listOf(
            Exercise(
                id = "EXERCISE-23-0001-001",
                name = "Push-ups",
                description = "A classic upper body exercise that targets chest, shoulders, and triceps",
                instructions = listOf(
                    "Start in a plank position with hands shoulder-width apart",
                    "Lower your body until your chest nearly touches the ground",
                    "Push back up to the starting position",
                    "Keep your core tight and body straight throughout"
                ),
                type = ExerciseType.PUSHUP,
                targetMuscles = listOf("Chest", "Shoulders", "Triceps", "Core"),
                difficulty = "BEGINNER"
            ),
            Exercise(
                id = "EXERCISE-23-0001-002",
                name = "Squats",
                description = "A fundamental lower body exercise that targets quadriceps, hamstrings, and glutes",
                instructions = listOf(
                    "Stand with feet shoulder-width apart",
                    "Lower your body by bending your knees and hips",
                    "Keep your back straight and chest up",
                    "Return to standing position"
                ),
                type = ExerciseType.SQUAT,
                targetMuscles = listOf("Quadriceps", "Hamstrings", "Glutes", "Core"),
                difficulty = "BEGINNER"
            )
        )
        return ClientResponse(
            success = true,
            data = exercises,
            message = "Using local exercise data"
        )
    }

    override suspend fun getExerciseDetails(exerciseId: String): ClientResponse<Exercise> = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserIdUseCase()
            val response = apiService.getExerciseDetails(exerciseId, userId.toString())
            if (response.isSuccessful) {
                val exercise = response.body()?.toDomain()
                if (exercise != null) {
                    ClientResponse(success = true, data = exercise, message = "Exercise details retrieved successfully")
                } else {
                    ClientResponse(
                        success = false,
                        data = null,
                        message = "Exercise not found"
                    )
                }
            } else {
                // Fallback to static data if API fails
                val staticExercises = getStaticExercises().data
                val exercise = staticExercises?.find { it.id == exerciseId }
                if (exercise != null) {
                    ClientResponse(success = true, data = exercise, message = "Using local exercise data")
                } else {
                    ClientResponse(
                        success = false,
                        data = null,
                        message = "Exercise not found: HTTP ${response.code()}"
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback to static data if API fails
            val staticExercises = getStaticExercises().data
            val exercise = staticExercises?.find { it.id == exerciseId }
            if (exercise != null) {
                ClientResponse(success = true, data = exercise, message = "Using local exercise data")
            } else {
                ClientResponse(
                    success = false,
                    data = null,
                    message = "Network error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    override suspend fun analyzePostureWithBackend(landmarks: List<List<List<Float>>>): ClientResponse<PostureResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.analyzePosture(AnalyzePostureRequestDto(landmarks = landmarks))
            if (response.isSuccessful) {
                val analysisResult = response.body()
                if (analysisResult != null) {
                    ClientResponse(success = true, data = analysisResult, message = "Posture analyzed successfully")
                } else {
                    ClientResponse(
                        success = false,
                        data = null,
                        message = "Empty response from server"
                    )
                }
            } else {
                ClientResponse(
                    success = false,
                    data = null,
                    message = "Failed to analyze posture: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ClientResponse(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun saveExerciseSession(sessionResult: ExerciseSessionResult): ClientResponse<Unit> = withContext(Dispatchers.IO) {
        try {
            val sessionDto = sessionResult.toDto()
            val response = apiService.saveExerciseSession(sessionDto)
            if (response.isSuccessful) {
                ClientResponse(success = true, data = Unit, message = "Session saved successfully")
            } else {
                ClientResponse(
                    success = false,
                    data = null,
                    message = "Failed to save session: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ClientResponse(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun getExerciseHistory(userId: String): ClientResponse<List<ExerciseSessionResult>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getExerciseHistory(userId)
            if (response.isSuccessful) {
                val sessions = response.body()?.map { it.toDomain() } ?: emptyList()
                ClientResponse(success = true, data = sessions, message = "History retrieved successfully")
            } else {
                ClientResponse(
                    success = false,
                    data = null,
                    message = "Failed to fetch history: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ClientResponse(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun getPerformanceSummary(userId: String, exerciseId: String?): ClientResponse<List<PerformanceSummaryDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPerformanceSummary(userId, exerciseId)
            if (response.isSuccessful) {
                val summaries = response.body() ?: emptyList()
                ClientResponse(success = true, data = summaries, message = "Performance summary retrieved")
            } else {
                ClientResponse(
                    success = false,
                    data = null,
                    message = "Failed to fetch performance summary: HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ClientResponse(success = false, data = null, message = "Network error: ${e.message ?: "Unknown error"}")
        }
    }
}

// Extension functions for data mapping
private fun ExerciseDto.toDomain(): Exercise {
    return Exercise(
        id = id,
        name = name,
        description = description,
        instructions = instructions,
        type = ExerciseType.valueOf(type.uppercase()),
        targetMuscles = targetMuscles ?: emptyList(), // Provide empty list as default
        difficulty = difficulty ?: "BEGINNER"  // Provide default difficulty
    )
}

private fun ExerciseSessionResult.toDto(): ExerciseSessionResultDto {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    return ExerciseSessionResultDto(
        sessionId = sessionId,
        userId = userId,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        startTime = startTime.format(formatter),
        endTime = endTime.format(formatter),
        durationMs = durationMs,
        repCount = repCount,
        averageAccuracy = averageAccuracy,
        issuesDetected = issuesDetected,
        classroomId = classroomId,
        taskId = taskId
    )
}

private fun ExerciseSessionResultDto.toDomain(): ExerciseSessionResult {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    return ExerciseSessionResult(
        sessionId = sessionId,
        userId = userId,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        startTime = LocalDateTime.parse(startTime, formatter),
        endTime = LocalDateTime.parse(endTime, formatter),
        durationMs = durationMs,
        repCount = repCount,
        averageAccuracy = averageAccuracy,
        issuesDetected = issuesDetected,
        classroomId = classroomId,
        taskId = taskId
    )
}