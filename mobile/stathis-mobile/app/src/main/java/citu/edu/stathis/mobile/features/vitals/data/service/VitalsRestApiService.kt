package citu.edu.stathis.mobile.features.vitals.data.service

import citu.edu.stathis.mobile.features.vitals.data.model.VitalsRestRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * REST API service for posting vital signs data
 * Uses the /api/vitals endpoint as specified in the backend
 */
interface VitalsRestApiService {
    @POST("api/vitals")
    suspend fun postVitals(@Body vitalsRequest: VitalsRestRequestDto): Response<Unit>
}

