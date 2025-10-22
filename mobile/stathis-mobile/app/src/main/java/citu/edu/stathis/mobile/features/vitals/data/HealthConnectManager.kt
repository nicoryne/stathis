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
import citu.edu.stathis.mobile.features.vitals.data.utils.BloodPressureEstimator
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

    private lateinit var healthConnectClient: HealthConnectClient
    private var isClientInitialized = false
    private val bloodPressureEstimator = BloodPressureEstimator()

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

    init {
        initializeHealthConnectClient()
    }

    private fun initializeHealthConnectClient() {
        try {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
            isClientInitialized = true
            Log.d(TAG, "Health Connect client initialized successfully.")
        } catch (e: Exception) {
            isClientInitialized = false
            _connectionState.value = ConnectionState.UNAVAILABLE
            Log.e(TAG, "Failed to initialize Health Connect client", e)
        }
    }

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
            _connectionState.value = ConnectionState.UNAVAILABLE
        }
        return available
    }

    suspend fun hasAllPermissions(): Boolean {
        Log.d(TAG, "Checking if all permissions are granted")
        if (!isClientInitialized) {
            Log.w(TAG, "Health Connect client not initialized.")
            return false
        }
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        val missingPermissions = permissions - grantedPermissions
        if (missingPermissions.isEmpty()) {
            Log.d(TAG, "All Health Connect permissions granted: $permissions")
            return true
        } else {
            Log.w(TAG, "Missing Health Connect permissions: $missingPermissions")
            return false
        }
    }

    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        Log.d(TAG, "Creating Health Connect permission request contract")
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun requestPermissionsAndConnect(): Boolean {
        Log.d(TAG, "Requesting permissions and attempting to connect")
        if (!isClientInitialized) {
            Log.e(TAG, "Health Connect client not available.")
            return false
        }
        
        if (!isHealthConnectAvailable()) {
            Log.w(TAG, "Health Connect SDK not available.")
            return false
        }
        
        try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            Log.d(TAG, "Current granted permissions: $grantedPermissions")
            
            if (grantedPermissions.containsAll(permissions)) {
                Log.d(TAG, "All permissions already granted, connecting...")
                connect()
                return true
            } else {
                Log.d(TAG, "Missing permissions: ${permissions - grantedPermissions}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            return false
        }
    }

    suspend fun onPermissionsGranted() {
        Log.d(TAG, "Permissions granted, attempting to reconnect")
        connect()
    }

    fun setUserId(userId: String) {
        currentUserId = userId
        Log.d(TAG, "User ID set: $userId")
    }

    suspend fun connect() {
        Log.v(TAG, "Attempting to connect to Health Connect")
        _connectionState.value = ConnectionState.CONNECTING
        
        if (!isClientInitialized) {
            Log.e(TAG, "Health Connect client not available.")
            _connectionState.value = ConnectionState.UNAVAILABLE
            return
        }
        
        if (!isHealthConnectAvailable()) {
            Log.w(TAG, "Health Connect SDK not available.")
            _connectionState.value = ConnectionState.UNAVAILABLE
            return
        }
        
        try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            Log.d(TAG, "Permissions check during connect: granted=$grantedPermissions")
            
            if (grantedPermissions.containsAll(permissions)) {
                Log.d(TAG, "All permissions granted, setting state to CONNECTED")
                _connectionState.value = ConnectionState.CONNECTED
                fetchLatestVitals()
            } else {
                Log.w(TAG, "Permissions not granted: Missing ${permissions - grantedPermissions}")
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during connection attempt", e)
            _connectionState.value = ConnectionState.UNAVAILABLE
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
            Log.w(TAG, "User ID not set. Using default user ID for vitals fetch.")
            // Use a default user ID if none is set
            currentUserId = "default_user"
        }
        
        if (!isClientInitialized) {
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
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            Log.d(TAG, "Permissions check before fetching vitals: granted=$grantedPermissions")
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
                healthConnectClient.readRecords(ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter))
            } catch (e: SecurityException) {
                Log.w(TAG, "SecurityException on heart rate read. Retrying permission check.", e)
                // Retry permission check
                val updatedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
                Log.d(TAG, "Retry permissions check: granted=$updatedPermissions")
                if (!updatedPermissions.containsAll(permissions)) {
                    Log.e(TAG, "Permissions still missing after retry: ${permissions - updatedPermissions}")
                    _connectionState.value = ConnectionState.DISCONNECTED
                    return
                }
                // Retry read
                healthConnectClient.readRecords(ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter))
            }

            val bloodPressureRecordsResult = healthConnectClient.readRecords(
                ReadRecordsRequest(BloodPressureRecord::class, timeRangeFilter)
            )
            val temperatureRecordsResult = healthConnectClient.readRecords(
                ReadRecordsRequest(BodyTemperatureRecord::class, timeRangeFilter)
            )
            val oxygenSaturationRecordsResult = healthConnectClient.readRecords(
                ReadRecordsRequest(OxygenSaturationRecord::class, timeRangeFilter)
            )
            val respiratoryRateRecordsResult = healthConnectClient.readRecords(
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
                val heartRate = latestHeartRateRecord?.samples?.lastOrNull()?.beatsPerMinute?.toInt() ?: 75
                
                // Use actual blood pressure if available, otherwise estimate from heart rate
                val (systolicBP, diastolicBP) = if (latestBloodPressureRecord != null) {
                    Pair(
                        latestBloodPressureRecord.systolic.inMillimetersOfMercury.toInt(),
                        latestBloodPressureRecord.diastolic.inMillimetersOfMercury.toInt()
                    )
                } else {
                    // Estimate blood pressure from heart rate
                    val estimatedBP = bloodPressureEstimator.estimateBloodPressure(
                        heartRate = heartRate,
                        age = 25, // TODO: Get actual user age
                        isResting = heartRate <= 100
                    )
                    Log.d(TAG, "Estimated blood pressure from heart rate $heartRate: ${estimatedBP.first}/${estimatedBP.second}")
                    estimatedBP
                }
                
                val vitalSignsData = VitalSigns(
                    userId = currentUserId ?: "default_user",
                    systolicBP = systolicBP,
                    diastolicBP = diastolicBP,
                    heartRate = heartRate,
                    respirationRate = latestRespiratoryRateRecord?.rate?.toInt() ?: 16,
                    temperature = latestTemperatureRecord?.temperature?.inCelsius?.toFloat() ?: 36.5f,
                    oxygenSaturation = latestOxygenSaturationRecord?.percentage?.value?.toFloat() ?: 98.0f,
                    timestamp = LocalDateTime.ofInstant(mostRecentRecordInstant, ZoneId.systemDefault()),
                    deviceName = "Health Connect",
                    isBloodPressureEstimated = latestBloodPressureRecord == null,
                    bloodPressureConfidence = if (latestBloodPressureRecord == null) {
                        bloodPressureEstimator.getEstimationConfidence(heartRate)
                    } else null
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