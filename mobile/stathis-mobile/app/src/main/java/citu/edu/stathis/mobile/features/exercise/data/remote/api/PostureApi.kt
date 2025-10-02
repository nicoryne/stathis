package citu.edu.stathis.mobile.features.exercise.data.remote.api

import citu.edu.stathis.mobile.features.exercise.data.remote.dto.ClassificationRequest
import citu.edu.stathis.mobile.features.exercise.data.remote.dto.ClassificationResultDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PostureApi {
    @POST("/api/posture/classify")
    suspend fun classify(@Body body: ClassificationRequest): ClassificationResultDto
}



