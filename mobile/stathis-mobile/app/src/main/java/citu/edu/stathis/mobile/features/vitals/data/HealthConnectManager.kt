package citu.edu.stathis.mobile.features.vitals.data.healthconnect

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import citu.edu.stathis.mobile.features.vitals.domain.model.VitalSigns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "HealthConnectManager"

    // Health Connect client
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    // States
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _vitalSigns = MutableStateFlow<VitalSigns?>(null)
    val vitalSigns: StateFlow<VitalSigns?> = _vitalSigns.asStateFlow()

    private var currentUserId: String? = null

    enum class ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING
    }

    // Health Connect permissions
    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class)
    )

    // Check if Health Connect is available on the device
    suspend fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.isProviderAvailable(context)
    }

    // Check if all required permissions are granted
    suspend fun hasAllPermissions(): Boolean {
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        return grantedPermissions.containsAll(permissions)
    }

    // Create permission launcher contract
    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    // Sets the user ID for the current session
    fun setUserId(userId: String) {
        currentUserId = userId
    }

    // Connect to Health Connect
    suspend fun connect() {
        _connectionState.value = ConnectionState.CONNECTING

        if (isHealthConnectAvailable() && hasAllPermissions()) {
            _connectionState.value = ConnectionState.CONNECTED
            fetchLatestVitals()
        } else {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    // Disconnect from Health Connect
    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        _vitalSigns.value = null
    }

    // Fetch latest vitals from Health Connect
    suspend fun fetchLatestVitals() {
        val userId = currentUserId ?: return

        try {
            // Time range for the last 24 hours
            val endTime = Instant.now()
            val startTime = endTime.minus(24, ChronoUnit.HOURS)
            val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

            // Fetch heart rate
            val heartRateRecords = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter
                )
            )

            // Fetch blood pressure
            val bloodPressureRecords = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    BloodPressureRecord::class,
                    timeRangeFilter
                )
            )

            // Fetch body temperature
            val temperatureRecords = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    BodyTemperatureRecord::class,
                    timeRangeFilter
                )
            )

            // Fetch oxygen saturation
            val oxygenSaturationRecords = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    OxygenSaturationRecord::class,
                    timeRangeFilter
                )
            )

            // Fetch respiratory rate
            val respiratoryRateRecords = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    RespiratoryRateRecord::class,
                    timeRangeFilter
                )
            )

            // Get the latest records
            val latestHeartRate = heartRateRecords.records.maxByOrNull { it.time }
            val latestBloodPressure = bloodPressureRecords.records.maxByOrNull { it.time }
            val latestTemperature = temperatureRecords.records.maxByOrNull { it.time }
            val latestOxygenSaturation = oxygenSaturationRecords.records.maxByOrNull { it.time }
            val latestRespiratoryRate = respiratoryRateRecords.records.maxByOrNull { it.time }

            // Create VitalSigns object
            if (latestHeartRate != null || latestBloodPressure != null ||
                latestTemperature != null || latestOxygenSaturation != null ||
                latestRespiratoryRate != null) {

                val vitalSigns = VitalSigns(
                    userId = userId,
                    systolicBP = latestBloodPressure?.systolicPressure?.inMillimetersOfMercury?.toInt() ?: 120,
                    diastolicBP = latestBloodPressure?.diastolicPressure?.inMillimetersOfMercury?.toInt() ?: 80,
                    heartRate = latestHeartRate?.samples?.lastOrNull()?.beatsPerMinute?.toInt() ?: 75,
                    respirationRate = latestRespiratoryRate?.rate?.toInt() ?: 16,
                    temperature = latestTemperature?.temperature?.inCelsius?.toFloat() ?: 36.5f,
                    oxygenSaturation = latestOxygenSaturation?.percentage?.toFloat() ?: 98.0f,
                    timestamp = LocalDateTime.now(),
                    deviceName = "Health Connect"
                )

                _vitalSigns.value = vitalSigns
                Log.d(TAG, "Fetched vital signs: $vitalSigns")
            } else {
                Log.d(TAG, "No vital signs data available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vital signs", e)
        }
    }

    // Clean up resources
    fun close() {
        _vitalSigns.value = null
    }
}
