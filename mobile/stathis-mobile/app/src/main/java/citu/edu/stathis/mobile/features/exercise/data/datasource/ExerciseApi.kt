package citu.edu.stathis.mobile.features.exercise.data.datasource

import citu.edu.stathis.mobile.features.exercise.domain.model.PostureAnalysis
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ExerciseApi {
    @POST("exercise/{exerciseId}/start")
    suspend fun startExerciseSession(@Path("exerciseId") exerciseId: String)

    @POST("exercise/{exerciseId}/end")
    suspend fun endExerciseSession(@Path("exerciseId") exerciseId: String)

    @POST("exercise/{exerciseId}/analysis")
    suspend fun saveAnalysisResult(
        @Path("exerciseId") exerciseId: String,
        @Body analysis: PostureAnalysis
    )
} 