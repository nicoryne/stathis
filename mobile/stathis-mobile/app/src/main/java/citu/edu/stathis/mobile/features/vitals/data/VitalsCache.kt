package citu.edu.stathis.mobile.features.vitals.data

import android.content.Context
import android.content.SharedPreferences
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VitalsCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vitals_cache", Context.MODE_PRIVATE)
    
    private val _cachedVitals = MutableStateFlow<VitalSigns?>(null)
    val cachedVitals: StateFlow<VitalSigns?> = _cachedVitals.asStateFlow()

    companion object {
        private const val KEY_HEART_RATE = "heart_rate"
        private const val KEY_SYSTOLIC_BP = "systolic_bp"
        private const val KEY_DIASTOLIC_BP = "diastolic_bp"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_OXYGEN_SATURATION = "oxygen_saturation"
        private const val KEY_RESPIRATION_RATE = "respiration_rate"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DEVICE_NAME = "device_name"
    }

    init {
        loadCachedVitals()
    }

    fun cacheVitals(vitalSigns: VitalSigns) {
        try {
            prefs.edit().apply {
                putInt(KEY_HEART_RATE, vitalSigns.heartRate)
                putInt(KEY_SYSTOLIC_BP, vitalSigns.systolicBP)
                putInt(KEY_DIASTOLIC_BP, vitalSigns.diastolicBP)
                putFloat(KEY_TEMPERATURE, vitalSigns.temperature)
                putFloat(KEY_OXYGEN_SATURATION, vitalSigns.oxygenSaturation)
                putInt(KEY_RESPIRATION_RATE, vitalSigns.respirationRate)
                putString(KEY_TIMESTAMP, vitalSigns.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                putString(KEY_USER_ID, vitalSigns.userId)
                putString(KEY_DEVICE_NAME, vitalSigns.deviceName)
                apply()
            }
            
            _cachedVitals.value = vitalSigns
        } catch (e: Exception) {
            // Handle any caching errors silently
        }
    }

    fun getCachedVitals(): VitalSigns? {
        return try {
            val heartRate = prefs.getInt(KEY_HEART_RATE, -1)
            val systolicBP = prefs.getInt(KEY_SYSTOLIC_BP, -1)
            val diastolicBP = prefs.getInt(KEY_DIASTOLIC_BP, -1)
            val temperature = prefs.getFloat(KEY_TEMPERATURE, -1f)
            val oxygenSaturation = prefs.getFloat(KEY_OXYGEN_SATURATION, -1f)
            val respirationRate = prefs.getInt(KEY_RESPIRATION_RATE, -1)
            val timestampStr = prefs.getString(KEY_TIMESTAMP, null)
            val userId = prefs.getString(KEY_USER_ID, null)
            val deviceName = prefs.getString(KEY_DEVICE_NAME, "Health Connect")

            if (heartRate == -1 || systolicBP == -1 || diastolicBP == -1 || 
                temperature == -1f || oxygenSaturation == -1f || respirationRate == -1 ||
                timestampStr == null || userId == null) {
                null
            } else {
                VitalSigns(
                    userId = userId,
                    systolicBP = systolicBP,
                    diastolicBP = diastolicBP,
                    heartRate = heartRate,
                    respirationRate = respirationRate,
                    temperature = temperature,
                    oxygenSaturation = oxygenSaturation,
                    timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    deviceName = deviceName
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun loadCachedVitals() {
        _cachedVitals.value = getCachedVitals()
    }

    fun clearCache() {
        prefs.edit().clear().apply()
        _cachedVitals.value = null
    }

    fun isCacheValid(maxAgeHours: Long = 24): Boolean {
        val cached = getCachedVitals() ?: return false
        val now = LocalDateTime.now()
        val cacheAge = java.time.Duration.between(cached.timestamp, now)
        return cacheAge.toHours() <= maxAgeHours
    }
}
