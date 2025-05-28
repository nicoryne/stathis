package citu.edu.stathis.mobile.features.vitals.data

import android.content.Context
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
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
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

    private val healthConnectClient by lazy {
        try {
            HealthConnectClient.getOrCreate(context).also {
                Log.d(TAG, "Health Connect client initialized successfully.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Health Connect client", e)
            null
        }
    }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _vitalSigns = MutableStateFlow<VitalSigns?>(null)
    val vitalSigns: StateFlow<VitalSigns?> = _vitalSigns.asStateFlow()

    private var currentUserId: String? = null

    enum class ConnectionState {
        CONNECTED,
        DISCONNECTED,
        CONNECTING,
        UNAVAILABLE
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class)
    )

    fun getSdkStatus(): Int {
        val status = HealthConnectClient.getSdkStatus(context)
        Log.d(TAG, "Health Connect SDK Status: $status")
        return status
    }

    fun isHealthConnectAvailable(): Boolean {
        val sdkStatus = getSdkStatus()
        val available = sdkStatus == HealthConnectClient.SDK_AVAILABLE
        if (!available) {
            Log.w(TAG, "Health Connect SDK not available. Status: $sdkStatus")
        }
        return available
    }

    suspend fun hasAllPermissions(): Boolean {
        if (healthConnectClient == null) {
            Log.w(TAG, "Health Connect client not initialized.")
            return false
        }
        val grantedPermissions = healthConnectClient!!.permissionController.getGrantedPermissions()
        val missingPermissions = permissions - grantedPermissions
        return if (missingPermissions.isEmpty()) {
            Log.d(TAG, "All Health Connect permissions granted: $permissions")
            true
        } else {
            Log.w(TAG, "Missing Health Connect permissions: $missingPermissions")
            false
        }
    }

    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    fun setUserId(userId: String) {
        currentUserId = userId
        Log.d(TAG, "User ID set: $userId")
    }

    suspend fun connect() {
        Log.v(TAG, "Attempting to connect to Health Connect")
        _connectionState.value = ConnectionState.CONNECTING
        if (healthConnectClient == null) {
            _connectionState.value = ConnectionState.UNAVAILABLE
            Log.e(TAG, "Health Connect client not available.")
            return
        }
        if (!isHealthConnectAvailable()) {
            _connectionState.value = ConnectionState.UNAVAILABLE
            Log.w(TAG, "Health Connect SDK not available.")
            return
        }
        val grantedPermissions = healthConnectClient!!.permissionController.getGrantedPermissions()
        if (grantedPermissions.containsAll(permissions)) {
            _connectionState.value = ConnectionState.CONNECTED
            Log.d(TAG, "All permissions granted: $grantedPermissions")
            fetchLatestVitals()
        } else {
            _connectionState.value = ConnectionState.DISCONNECTED
            Log.w(TAG, "Permissions not granted: Missing ${permissions - grantedPermissions}")
        }
    }

    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        _vitalSigns.value = null
        Log.d(TAG, "Disconnected from Health Connect.")
    }

    suspend fun fetchLatestVitals() {
        val userId = currentUserId
        if (userId == null) {
            Log.w(TAG, "User ID not set. Cannot fetch vitals.")
            _vitalSigns.value = null
            return
        }
        if (healthConnectClient == null) {
            Log.w(TAG, "Health Connect client not initialized.")
            _vitalSigns.value = null
            return
        }
        if (connectionState.value != ConnectionState.CONNECTED) {
            Log.w(TAG, "Not connected to Health Connect. Cannot fetch vitals.")
            return
        }

        try {
            // Verify permissions before reading
            var grantedPermissions = healthConnectClient!!.permissionController.getGrantedPermissions()
            if (!grantedPermissions.containsAll(permissions)) {
                Log.e(TAG, "Required permissions missing: ${permissions - grantedPermissions}")
                _connectionState.value = ConnectionState.DISCONNECTED
                return
            }

            val endTime = Instant.now()
            val startTime = endTime.minus(24, ChronoUnit.HOURS)
            val timeRangeFilter = TimeRangeFilter.between(startTime, endTime)

            Log.d(TAG, "Fetching vital signs for time range: $startTime to $endTime")

            val heartRateRecordsResult = try {
                healthConnectClient!!.readRecords(ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter))
            } catch (e: SecurityException) {
                Log.w(TAG, "SecurityException on heart rate read. Retrying permission check.", e)
                // Retry permission check
                grantedPermissions = healthConnectClient!!.permissionController.getGrantedPermissions()
                if (!grantedPermissions.containsAll(permissions)) {
                    Log.e(TAG, "Permissions still missing after retry: ${permissions - grantedPermissions}")
                    _connectionState.value = ConnectionState.DISCONNECTED
                    return
                }
                // Retry read
                healthConnectClient!!.readRecords(ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter))
            }

            val bloodPressureRecordsResult = healthConnectClient!!.readRecords(
                ReadRecordsRequest(BloodPressureRecord::class, timeRangeFilter)
            )
            val temperatureRecordsResult = healthConnectClient!!.readRecords(
                ReadRecordsRequest(BodyTemperatureRecord::class, timeRangeFilter)
            )
            val oxygenSaturationRecordsResult = healthConnectClient!!.readRecords(
                ReadRecordsRequest(OxygenSaturationRecord::class, timeRangeFilter)
            )
            val respiratoryRateRecordsResult = healthConnectClient!!.readRecords(
                ReadRecordsRequest(RespiratoryRateRecord::class, timeRangeFilter)
            )

            val latestHeartRateRecord = heartRateRecordsResult.records.maxByOrNull { it.samples.lastOrNull()?.time ?: it.startTime }
            val latestBloodPressureRecord = bloodPressureRecordsResult.records.maxByOrNull { it.time }
            val latestTemperatureRecord = temperatureRecordsResult.records.maxByOrNull { it.time }
            val latestOxygenSaturationRecord = oxygenSaturationRecordsResult.records.maxByOrNull { it.time }
            val latestRespiratoryRateRecord = respiratoryRateRecordsResult.records.maxByOrNull { it.time }

            val allRecordInstants = listOfNotNull(
                latestHeartRateRecord?.samples?.lastOrNull()?.time ?: latestHeartRateRecord?.startTime,
                latestBloodPressureRecord?.time,
                latestTemperatureRecord?.time,
                latestOxygenSaturationRecord?.time,
                latestRespiratoryRateRecord?.time
            )
            val mostRecentRecordInstant = allRecordInstants.maxOrNull()

            if (mostRecentRecordInstant != null) {
                val vitalSignsData = VitalSigns(
                    userId = userId,
                    systolicBP = latestBloodPressureRecord?.systolic?.inMillimetersOfMercury?.toInt() ?: 120,
                    diastolicBP = latestBloodPressureRecord?.diastolic?.inMillimetersOfMercury?.toInt() ?: 80,
                    heartRate = latestHeartRateRecord?.samples?.lastOrNull()?.beatsPerMinute?.toInt() ?: 75,
                    respirationRate = latestRespiratoryRateRecord?.rate?.toInt() ?: 16,
                    temperature = latestTemperatureRecord?.temperature?.inCelsius?.toFloat() ?: 36.5f,
                    oxygenSaturation = latestOxygenSaturationRecord?.percentage?.value?.toFloat() ?: 98.0f,
                    timestamp = LocalDateTime.ofInstant(mostRecentRecordInstant, ZoneId.systemDefault()),
                    deviceName = "Health Connect"
                )
                _vitalSigns.value = vitalSignsData
                Log.d(TAG, "Fetched vital signs: $vitalSignsData")
            } else {
                Log.d(TAG, "No vital signs data available in the last 24 hours.")
                _vitalSigns.value = null
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission error fetching vital signs. Disconnecting.", e)
            _connectionState.value = ConnectionState.DISCONNECTED
            _vitalSigns.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vital signs", e)
            _vitalSigns.value = null
        }
    }

    fun clearLocalVitalsData() {
        _vitalSigns.value = null
        Log.d(TAG, "Local vitals data cleared.")
    }
}