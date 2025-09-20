package citu.edu.stathis.mobile.features.vitals.domain

import citu.edu.stathis.mobile.features.vitals.data.model.VitalsRequestDto
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VitalsApiService {
    @POST("api/vitals")
    suspend fun saveVitals(@Body vitalsRequest: VitalsRequestDto): Response<Unit>

    @GET("api/vitals/history/{userId}")
    suspend fun getVitalsHistory(@Path("userId") userId: String): Response<List<VitalsResponseDto>>

    @DELETE("api/vitals/{recordId}")
    suspend fun deleteVitalRecord(@Path("recordId") recordId: String): Response<Unit>
}