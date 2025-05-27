package citu.edu.stathis.mobile.features.vitals.ui

import android.bluetooth.BluetoothDevice // Keep if you retain direct BLE device interaction (e.g. for names)
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.healthconnect.HealthConnectManager // For ConnectionState enum
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.model.HealthRiskAlert
import citu.edu.stathis.mobile.features.vitals.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// New UI State representations
sealed class VitalsRealTimeUiState {
    data object Initial : VitalsRealTimeUiState()
    data object Loading : VitalsRealTimeUiState() // When actively fetching/connecting
    data class Data(val vitalSigns: VitalSigns, val alert: HealthRiskAlert? = null) : VitalsRealTimeUiState()
    data class Error(val message: String) : VitalsRealTimeUiState()
}

sealed class VitalsHistoryUiState {
    data object Loading : VitalsHistoryUiState()
    data class Data(val vitalsList: List<VitalSigns>) : VitalsHistoryUiState()
    data object Empty : VitalsHistoryUiState()
    data class Error(val message: String) : VitalsHistoryUiState()
}

sealed class HealthConnectUiState {
    data object NotAvailable : HealthConnectUiState() // Health Connect app not installed
    data object PermissionsNotGranted : HealthConnectUiState()
    data object AvailableAndConnected : HealthConnectUiState()
    data object AvailableButDisconnected : HealthConnectUiState() // Available, permissions granted, but not "connected"
    data object Connecting : HealthConnectUiState()
    data class Error(val message: String) : HealthConnectUiState()
}

@HiltViewModel
class VitalsViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase, // Assuming you create this
    private val checkHealthConnectAvailabilityUseCase: CheckHealthConnectAvailabilityUseCase,
    private val requestHealthConnectPermissionsUseCase: RequestHealthConnectPermissionsUseCase,
    private val connectToHealthConnectUseCase: ConnectToHealthConnectUseCase,
    private val monitorRealTimeVitalsUseCase: MonitorRealTimeVitalsUseCase,
    private val detectHealthRiskUseCase: DetectHealthRiskUseCase,
    private val saveVitalsUseCase: SaveVitalsUseCase,
    private val getVitalsHistoryUseCase: GetVitalsHistoryUseCase,
    private val deleteVitalRecordUseCase: DeleteVitalRecordUseCase
    // Add other use cases as needed
) : ViewModel() {

    private val _realTimeState = MutableStateFlow<VitalsRealTimeUiState>(VitalsRealTimeUiState.Initial)
    val realTimeState: StateFlow<VitalsRealTimeUiState> = _realTimeState.asStateFlow()

    private val _historyState = MutableStateFlow<VitalsHistoryUiState>(VitalsHistoryUiState.Loading)
    val historyState: StateFlow<VitalsHistoryUiState> = _historyState.asStateFlow()

    private val _healthConnectState = MutableStateFlow<HealthConnectUiState>(HealthConnectUiState.NotAvailable)
    val healthConnectState: StateFlow<HealthConnectUiState> = _healthConnectState.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            // currentUserId = getCurrentUserIdUseCase() // Fetch and set user ID
            // For now, using a placeholder or assuming it's passed if needed
            // currentUserId = "test-user" // Replace with actual user ID logic

            // The init block of your old VM had supabaseClient.auth.currentUserOrNull()?.id
            // This logic needs to be replaced by an auth-aware component.
            // For now, let's assume userId will be set before other operations.
        }
        checkHealthConnectStatus()
    }

    fun initializeForUser(userId: String) {
        currentUserId = userId
        loadVitalsHistory()
        startMonitoringVitals()
    }

    fun checkHealthConnectStatus() {
        viewModelScope.launch {
            val availability = checkHealthConnectAvailabilityUseCase() // This use case would return a specific state.
            // Based on availability (e.g. using HealthConnectManager.isHealthConnectAvailable()
            // and HealthConnectManager.hasAllPermissions() through the use case)
            // Update _healthConnectState accordingly.
            // Example:
            // if (!availability.isAvailable) {
            // _healthConnectState.value = HealthConnectUiState.NotAvailable
            // return@launch
            // }
            // if (!availability.hasPermissions) {
            // _healthConnectState.value = HealthConnectUiState.PermissionsNotGranted
            // return@launch
            // }
            // _healthConnectState.value = HealthConnectUiState.AvailableButDisconnected // Or trigger connect
        }
    }

    fun requestPermissions(permissionResultLauncher: (Set<String>) -> Unit) {
        // val contract = requestHealthConnectPermissionsUseCase.createContract()
        // val permissions = requestHealthConnectPermissionsUseCase.getPermissionsSet()
        // permissionResultLauncher.launch(permissions) // Screen would handle this.
        // ViewModel would then react to permission grant/denial.
    }

    fun connectToHealthService() {
        viewModelScope.launch {
            _healthConnectState.value = HealthConnectUiState.Connecting
            // val result = connectToHealthConnectUseCase()
            // Update _healthConnectState based on result.
            // If successful, start monitoring.
        }
    }


    private fun startMonitoringVitals() {
        val userId = currentUserId ?: return
        _realTimeState.value = VitalsRealTimeUiState.Loading
        viewModelScope.launch {
            monitorRealTimeVitalsUseCase.invoke(userId).collectLatest { vitalSigns ->
                if (vitalSigns != null) {
                    val alert = detectHealthRiskUseCase(vitalSigns, VitalsThresholds()) // Pass relevant thresholds
                    _realTimeState.value = VitalsRealTimeUiState.Data(vitalSigns, alert)
                } else {
                    // Could be initial state or if flow emits null after disconnection
                    if (_healthConnectState.value is HealthConnectUiState.AvailableAndConnected) {
                        // If we are supposed to be connected but get null, maybe it's an intermittent issue
                        // Or simply means no new data yet.
                    }
                }
            }
        }
    }
    // Add a function to be called by UI to manually refresh vitals if needed by the use case
    fun triggerVitalsRefresh() {
        viewModelScope.launch {
            // monitorRealTimeVitalsUseCase.refreshVitals() // If this method exists
        }
    }

    fun saveCurrentVitals() {
        val currentVitalsState = _realTimeState.value
        if (currentVitalsState is VitalsRealTimeUiState.Data) {
            val vitalsToSave = currentVitalsState.vitalSigns
            // Add classroomId, taskId from current app context if available
            // val enrichedVitals = vitalsToSave.copy(classroomId = "currentClass", taskId = "currentTask")
            viewModelScope.launch {
                val result = saveVitalsUseCase(vitalsToSave /* or enrichedVitals */)
                if (result.success) {
                    loadVitalsHistory() // Refresh history
                } else {
                    // Handle save error, e.g., emit an event
                }
            }
        }
    }

    fun loadVitalsHistory() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _historyState.value = VitalsHistoryUiState.Loading
            getVitalsHistoryUseCase(userId).collectLatest { result ->
                if (result.success && result.data != null) {
                    _historyState.value = if (result.data.isNotEmpty()) {
                        VitalsHistoryUiState.Data(result.data)
                    } else {
                        VitalsHistoryUiState.Empty
                    }
                } else {
                    _historyState.value = VitalsHistoryUiState.Error(result.message)
                }
            }
        }
    }

    fun deleteVitalRecord(recordId: String) {
        viewModelScope.launch {
            val result = deleteVitalRecordUseCase(recordId)
            if (result.success) {
                loadVitalsHistory() // Refresh history
            } else {
                // Handle delete error
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Disconnect from Health Connect if use case requires explicit disconnection
        // disconnectFromHealthConnectUseCase()
    }
}