package citu.edu.stathis.mobile.features.exercise.domain

import citu.edu.stathis.mobile.features.exercise.data.model.AnalyzePostureRequestDto
import citu.edu.stathis.mobile.features.exercise.data.model.ExerciseDto
import citu.edu.stathis.mobile.features.exercise.data.model.ExerciseSessionResultDto
import citu.edu.stathis.mobile.features.exercise.data.model.PerformanceSummaryDto
import citu.edu.stathis.mobile.features.exercise.data.model.PostureResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExerciseApiService {
    @GET("api/exercises")
    suspend fun getAvailableExercises(): Response<List<ExerciseDto>>

    @GET("api/exercises/{id}")
    suspend fun getExerciseDetails(@Path("id") exerciseId: String): Response<ExerciseDto>

    @POST("api/posture/analyze")
    suspend fun analyzePosture(@Body request: AnalyzePostureRequestDto): Response<PostureResponseDto>

    @POST("api/exercise/sessions")
    suspend fun saveExerciseSession(@Body sessionResult: ExerciseSessionResultDto): Response<Unit>

    @GET("api/exercise/history/{userId}")
    suspend fun getExerciseHistory(@Path("userId") userId: String): Response<List<ExerciseSessionResultDto>>

    @GET("api/exercise/performance/{userId}")
    suspend fun getPerformanceSummary(
        @Path("userId") userId: String,
        @Query("exerciseId") exerciseId: String?
    ): Response<List<PerformanceSummaryDto>>
}