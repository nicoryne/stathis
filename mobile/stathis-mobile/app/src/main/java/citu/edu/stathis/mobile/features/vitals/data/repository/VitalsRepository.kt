package citu.edu.stathis.mobile.features.vitals.data.repository

import citu.edu.stathis.mobile.features.vitals.domain.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.domain.repository.IVitalsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class VitalsRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : IVitalsRepository {

    @Serializable
    data class VitalSignsDto(
        val id: String? = null,
        val user_id: String,
        val bp_sys: Int,
        val bp_dia: Int,
        val heartrate: Int,
        val respirate: Int,
        val temp: Float,
        val o2sat: Float,
        val timestamp: String,
        val device_name: String? = null
    )

    override suspend fun saveVitalSigns(vitalSigns: VitalSigns): Result<Unit> {
        return try {
            val userId = vitalSigns.userId
            val timestamp = vitalSigns.timestamp.format(DateTimeFormatter.ISO_DATE_TIME)

            supabaseClient.postgrest["vitals"].insert(
                VitalSignsDto(
                    user_id = userId,
                    bp_sys = vitalSigns.systolicBP,
                    bp_dia = vitalSigns.diastolicBP,
                    heartrate = vitalSigns.heartRate,
                    respirate = vitalSigns.respirationRate,
                    temp = vitalSigns.temperature,
                    o2sat = vitalSigns.oxygenSaturation,
                    timestamp = timestamp,
                    device_name = vitalSigns.deviceName
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVitalSignsHistory(userId: String): Flow<List<VitalSigns>> = flow {
        try {
            val response = supabaseClient.postgrest["vitals"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("timestamp", Order.DESCENDING)
                }
                .decodeList<VitalSignsDto>()

            emit(response.map { it.toVitalSigns() })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getLatestVitalSigns(userId: String): Flow<VitalSigns?> = flow {
        try {
            val response = supabaseClient.postgrest["vitals"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("timestamp", Order.DESCENDING)
                    limit(1)
                }
                .decodeList<VitalSignsDto>()

            emit(response.firstOrNull()?.toVitalSigns())
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun deleteVitalSigns(id: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["vitals"]
                .delete {
                    filter {
                        eq("id", id)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun VitalSignsDto.toVitalSigns(): VitalSigns {
        val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
        return VitalSigns(
            id = id,
            userId = user_id,
            systolicBP = bp_sys,
            diastolicBP = bp_dia,
            heartRate = heartrate,
            respirationRate = respirate,
            temperature = temp,
            oxygenSaturation = o2sat,
            timestamp = LocalDateTime.parse(timestamp, dateFormatter),
            deviceName = device_name
        )
    }
}