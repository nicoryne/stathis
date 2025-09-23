package citu.edu.stathis.mobile.features.vitals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.model.HealthRiskAlert
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsThresholds
import citu.edu.stathis.mobile.features.vitals.domain.usecase.*
import citu.edu.stathis.mobile.features.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

sealed class VitalsRealTimeUiState {
    data object Initial : VitalsRealTimeUiState()
    data object Loading : VitalsRealTimeUiState()
    data class Data(val vitalSigns: VitalSigns, val alert: HealthRiskAlert? = null) : VitalsRealTimeUiState()
    data class NoData(val message: String) : VitalsRealTimeUiState()
    data class Error(val message: String) : VitalsRealTimeUiState()
}

sealed class VitalsHistoryUiState {
    data object Loading : VitalsHistoryUiState()
    data class Data(val vitalsList: List<VitalSigns>) : VitalsHistoryUiState()
    data object Empty : VitalsHistoryUiState()
    data class Error(val message: String) : VitalsHistoryUiState()
}

sealed class HealthConnectUiState {
    data object Initial : HealthConnectUiState()
    data object ClientNotAvailable : HealthConnectUiState()
    data object PermissionsNotGranted : HealthConnectUiState()
    data object AvailableAndConnected : HealthConnectUiState()
    data object AvailableButDisconnected : HealthConnectUiState()
    data object Connecting : HealthConnectUiState()
    data class Error(val message: String) : HealthConnectUiState()
}

sealed class VitalsViewEvent {
    data class ShowSnackbar(val message: String) : VitalsViewEvent()
    data object RequestHealthConnectPermissions : VitalsViewEvent()
    data class HandleAlertAction(val alert: HealthRiskAlert) : VitalsViewEvent()
}

@HiltViewModel
class VitalsViewModel @Inject constructor(
    internal val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val checkHealthConnectAvailabilityUseCase: CheckHealthConnectAvailabilityUseCase,
    val requestHealthConnectPermissionsUseCase: RequestHealthConnectPermissionsUseCase,
    private val connectToHealthConnectUseCase: ConnectToHealthConnectUseCase,
    private val monitorRealTimeVitalsUseCase: MonitorRealTimeVitalsUseCase,
    private val detectHealthRiskUseCase: DetectHealthRiskUseCase,
    private val saveVitalsUseCase: SaveVitalsUseCase,
    private val getVitalsHistoryUseCase: GetVitalsHistoryUseCase,
    private val saveVitalsResultUseCase: SaveVitalsResultUseCase,
    private val getVitalsHistoryResultUseCase: GetVitalsHistoryResultUseCase,
    private val deleteVitalRecordUseCase: DeleteVitalRecordUseCase,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _realTimeState = MutableStateFlow<VitalsRealTimeUiState>(VitalsRealTimeUiState.Initial)
    val realTimeState: StateFlow<VitalsRealTimeUiState> = _realTimeState.asStateFlow()

    private val _historyState = MutableStateFlow<VitalsHistoryUiState>(VitalsHistoryUiState.Loading)
    val historyState: StateFlow<VitalsHistoryUiState> = _historyState.asStateFlow()

    private val _healthConnectState = MutableStateFlow<HealthConnectUiState>(HealthConnectUiState.Initial)
    val healthConnectState: StateFlow<HealthConnectUiState> = _healthConnectState.asStateFlow()

    private val _events = MutableSharedFlow<VitalsViewEvent>()
    val events: SharedFlow<VitalsViewEvent> = _events.asSharedFlow()

    private var currentUserId: String? = null
    private var vitalsMonitoringJob: Job? = null
    private var periodicRefreshJob: Job? = null

    private val POLLING_INTERVAL_MS = 10000L

    init {
        viewModelScope.launch {
            Log.d("VitalsViewModel", "Initializing VitalsViewModel")
            currentUserId = getCurrentUserIdUseCase()
            if (currentUserId == null) {
                Log.e("VitalsViewModel", "User ID is null")
                _healthConnectState.value = HealthConnectUiState.Error("User not identified. Cannot proceed.")
                _events.emit(VitalsViewEvent.ShowSnackbar("User not identified. Please log in again."))
                return@launch
            }
            Log.d("VitalsViewModel", "User ID set: $currentUserId")
            monitorRealTimeVitalsUseCase.setUserIdForMonitoring(currentUserId!!)
            monitorHealthConnectManagerConnectionState()
            checkHealthConnectStatus()
        }
    }

    private fun monitorHealthConnectManagerConnectionState() {
        viewModelScope.launch {
            healthConnectManager.connectionState.collect { state ->
                Log.d("VitalsViewModel", "HealthConnectManager connection state changed: $state")
                when (state) {
                    HealthConnectManager.ConnectionState.CONNECTED -> {
                        _healthConnectState.value = HealthConnectUiState.AvailableAndConnected
                        initializeUserSpecificFeatures()
                    }
                    HealthConnectManager.ConnectionState.DISCONNECTED -> {
                        val availability = checkHealthConnectAvailabilityUseCase()
                        if (!availability.isClientAvailable) {
                            _healthConnectState.value = HealthConnectUiState.ClientNotAvailable
                        } else if (!availability.hasAllPermissions) {
                            _healthConnectState.value = HealthConnectUiState.PermissionsNotGranted
                        } else {
                            _healthConnectState.value = HealthConnectUiState.AvailableButDisconnected
                        }
                        stopVitalsMonitoringAndPolling()
                    }
                    HealthConnectManager.ConnectionState.CONNECTING -> {
                        _healthConnectState.value = HealthConnectUiState.Connecting
                    }
                    HealthConnectManager.ConnectionState.UNAVAILABLE -> {
                        _healthConnectState.value = HealthConnectUiState.AvailableButDisconnected
                    }
                }
            }
        }
    }

    internal fun initializeUserSpecificFeatures() {
        currentUserId?.let {
            Log.d("VitalsViewModel", "Initializing user-specific features for user: $it")
            loadVitalsHistory()
            startMonitoringVitals()
            startPeriodicVitalsRefresh()
        }
    }

    fun checkHealthConnectStatus() {
        viewModelScope.launch {
            Log.d("VitalsViewModel", "Checking Health Connect status")
            val availability = checkHealthConnectAvailabilityUseCase()
            Log.d("VitalsViewModel", "Health Connect availability: isClientAvailable=${availability.isClientAvailable}, hasAllPermissions=${availability.hasAllPermissions}")
            if (!availability.isClientAvailable) {
                _healthConnectState.value = HealthConnectUiState.ClientNotAvailable
            } else if (!availability.hasAllPermissions) {
                _healthConnectState.value = HealthConnectUiState.PermissionsNotGranted
                Log.d("VitalsViewModel", "Emitting RequestHealthConnectPermissions event")
                _events.emit(VitalsViewEvent.RequestHealthConnectPermissions)
            } else {
                if (healthConnectManager.connectionState.value != HealthConnectManager.ConnectionState.CONNECTED &&
                    healthConnectManager.connectionState.value != HealthConnectManager.ConnectionState.CONNECTING) {
                    _healthConnectState.value = HealthConnectUiState.AvailableButDisconnected
                }
            }
        }
    }

    fun onPermissionsResult(grantedPermissions: Set<String>) {
        viewModelScope.launch {
            Log.d("VitalsViewModel", "Handling permissions result: $grantedPermissions")
            val requestedPermissions = requestHealthConnectPermissionsUseCase.getPermissionsSet()
            Log.d("VitalsViewModel", "Requested permissions: $requestedPermissions")
            if (grantedPermissions.containsAll(requestedPermissions)) {
                Log.d("VitalsViewModel", "All permissions granted, connecting to Health Connect")
                connectToHealthService()
            } else {
                Log.w("VitalsViewModel", "Not all permissions granted. Missing: ${requestedPermissions - grantedPermissions}")
                _healthConnectState.value = HealthConnectUiState.PermissionsNotGranted
                _events.emit(VitalsViewEvent.ShowSnackbar("Required Health Connect permissions were not granted."))
            }
        }
    }

    fun connectToHealthService() {
        viewModelScope.launch {
            Log.d("VitalsViewModel", "Attempting to connect to Health Connect service")
            val availability = checkHealthConnectAvailabilityUseCase()
            Log.d("VitalsViewModel", "Connect attempt - availability: isClientAvailable=${availability.isClientAvailable}, hasAllPermissions=${availability.hasAllPermissions}")
            if (!availability.isClientAvailable) {
                Log.e("VitalsViewModel", "Health Connect app not available")
                _healthConnectState.value = HealthConnectUiState.ClientNotAvailable
                _events.emit(VitalsViewEvent.ShowSnackbar("Health Connect app is not available on this device."))
                return@launch
            }
            if (!availability.hasAllPermissions) {
                Log.w("VitalsViewModel", "Permissions not granted, requesting again")
                _healthConnectState.value = HealthConnectUiState.PermissionsNotGranted
                _events.emit(VitalsViewEvent.RequestHealthConnectPermissions)
                return@launch
            }
            Log.d("VitalsViewModel", "Calling connectToHealthConnectUseCase")
            connectToHealthConnectUseCase()
        }
    }

    private fun startMonitoringVitals() {
        if (currentUserId == null || _healthConnectState.value !is HealthConnectUiState.AvailableAndConnected) return

        vitalsMonitoringJob?.cancel()
        _realTimeState.value = VitalsRealTimeUiState.Loading
        vitalsMonitoringJob = viewModelScope.launch {
            monitorRealTimeVitalsUseCase.invoke().collectLatest { vitalSigns ->
                if (vitalSigns != null) {
                    val updatedVitalsWithUserId = vitalSigns.copy(userId = currentUserId!!)
                    val alert = detectHealthRiskUseCase(updatedVitalsWithUserId, VitalsThresholds())
                    _realTimeState.value = VitalsRealTimeUiState.Data(updatedVitalsWithUserId, alert)
                    if (alert != null) {
                        _events.emit(VitalsViewEvent.HandleAlertAction(alert))
                    }
                } else if (_healthConnectState.value is HealthConnectUiState.AvailableAndConnected) {
                    _realTimeState.value = VitalsRealTimeUiState.NoData("Waiting for new vital signs data from Health Connect. Ensure your watch is syncing.")
                }
            }
        }
    }

    private fun startPeriodicVitalsRefresh() {
        if (currentUserId == null || _healthConnectState.value !is HealthConnectUiState.AvailableAndConnected) return

        periodicRefreshJob?.cancel()
        periodicRefreshJob = viewModelScope.launch {
            while (true) {
                monitorRealTimeVitalsUseCase.refreshVitals()
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private fun stopVitalsMonitoringAndPolling() {
        vitalsMonitoringJob?.cancel()
        periodicRefreshJob?.cancel()
        if (_realTimeState.value !is VitalsRealTimeUiState.Error) {
            _realTimeState.value = VitalsRealTimeUiState.Initial
        }
    }

    fun triggerVitalsRefresh() {
        viewModelScope.launch {
            if (_healthConnectState.value is HealthConnectUiState.AvailableAndConnected) {
                _events.emit(VitalsViewEvent.ShowSnackbar("Refreshing vitals data..."))
                monitorRealTimeVitalsUseCase.refreshVitals()
            } else {
                _events.emit(VitalsViewEvent.ShowSnackbar("Health Connect is not connected. Cannot refresh."))
            }
        }
    }

    fun saveCurrentVitals(
        classroomId: String? = null,
        taskId: String? = null,
        isPreActivity: Boolean? = null,
        isPostActivity: Boolean? = null
    ) {
        val currentVitalsState = _realTimeState.value
        if (currentVitalsState is VitalsRealTimeUiState.Data) {
            viewModelScope.launch {
                val result = saveVitalsResultUseCase(
                    vitalSignsFromHealthConnect = currentVitalsState.vitalSigns,
                    classroomId = classroomId,
                    taskId = taskId,
                    isPreActivity = isPreActivity,
                    isPostActivity = isPostActivity
                )
                when (result) {
                    is Result.Success -> {
                        _events.emit(VitalsViewEvent.ShowSnackbar("Vitals saved successfully."))
                        loadVitalsHistory()
                    }
                    is Result.Error -> {
                        _events.emit(VitalsViewEvent.ShowSnackbar("Failed to save vitals: ${result.message}"))
                    }
                }
            }
        } else {
            viewModelScope.launch {
                _events.emit(VitalsViewEvent.ShowSnackbar("No current vitals data to save."))
            }
        }
    }

    fun loadVitalsHistory() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _historyState.value = VitalsHistoryUiState.Loading
            getVitalsHistoryResultUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val data = result.data
                        _historyState.value = if (data.isNotEmpty()) {
                            VitalsHistoryUiState.Data(data)
                        } else {
                            VitalsHistoryUiState.Empty
                        }
                    }
                    is Result.Error -> {
                        _historyState.value = VitalsHistoryUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun deleteVitalRecord(recordId: String) {
        viewModelScope.launch {
            val result = deleteVitalRecordUseCase(recordId)
            if (result.success) {
                _events.emit(VitalsViewEvent.ShowSnackbar("Vital record deleted."))
                loadVitalsHistory()
            } else {
                _events.emit(VitalsViewEvent.ShowSnackbar("Failed to delete record: ${result.message}"))
            }
        }
    }

    fun handleAlertAction(alert: HealthRiskAlert) {
        viewModelScope.launch {
            _events.emit(VitalsViewEvent.ShowSnackbar("Action for: ${alert.riskType} - ${alert.suggestedAction ?: "Monitor closely."}"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        healthConnectManager.disconnect()
        stopVitalsMonitoringAndPolling()
    }
}