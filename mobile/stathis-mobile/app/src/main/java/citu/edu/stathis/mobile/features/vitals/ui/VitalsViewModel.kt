package citu.edu.stathis.mobile.features.vitals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.model.HealthRiskAlert
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsThresholds
import citu.edu.stathis.mobile.features.vitals.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
            currentUserId = getCurrentUserIdUseCase()
            if (currentUserId == null) {
                _healthConnectState.value = HealthConnectUiState.Error("User not identified. Cannot proceed.")
                _events.emit(VitalsViewEvent.ShowSnackbar("User not identified. Please log in again."))
                return@launch
            }
            monitorRealTimeVitalsUseCase.setUserIdForMonitoring(currentUserId!!)
            monitorHealthConnectManagerConnectionState()
            checkHealthConnectStatus()
        }
    }

    private fun monitorHealthConnectManagerConnectionState() {
        viewModelScope.launch {
            healthConnectManager.connectionState.collect { state ->
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
            loadVitalsHistory()
            startMonitoringVitals()
            startPeriodicVitalsRefresh()
        }
    }

    fun checkHealthConnectStatus() {
        viewModelScope.launch {
            val availability = checkHealthConnectAvailabilityUseCase()
            if (!availability.isClientAvailable) {
                _healthConnectState.value = HealthConnectUiState.ClientNotAvailable
            } else if (!availability.hasAllPermissions) {
                _healthConnectState.value = HealthConnectUiState.PermissionsNotGranted
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
            val requestedPermissions = requestHealthConnectPermissionsUseCase.getPermissionsSet()
            if (grantedPermissions.containsAll(requestedPermissions)) {
                connectToHealthService()
            } else {
                _healthConnectState.value = HealthConnectUiState.PermissionsNotGranted
                _events.emit(VitalsViewEvent.ShowSnackbar("Required Health Connect permissions were not granted."))
            }
        }
    }

    fun connectToHealthService() {
        viewModelScope.launch {
            val availability = checkHealthConnectAvailabilityUseCase()
            if (!availability.isClientAvailable) {
                _healthConnectState.value = HealthConnectUiState.ClientNotAvailable
                _events.emit(VitalsViewEvent.ShowSnackbar("Health Connect app is not available on this device."))
                return@launch
            }
            if (!availability.hasAllPermissions) {
                _healthConnectState.value = HealthConnectUiState.PermissionsNotGranted
                _events.emit(VitalsViewEvent.RequestHealthConnectPermissions)
                return@launch
            }
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
                val result = saveVitalsUseCase(
                    vitalSignsFromHealthConnect = currentVitalsState.vitalSigns,
                    classroomId = classroomId,
                    taskId = taskId,
                    isPreActivity = isPreActivity,
                    isPostActivity = isPostActivity
                )
                if (result.success) {
                    _events.emit(VitalsViewEvent.ShowSnackbar("Vitals saved successfully."))
                    loadVitalsHistory()
                } else {
                    _events.emit(VitalsViewEvent.ShowSnackbar("Failed to save vitals: ${result.message}"))
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
            getVitalsHistoryUseCase()?.collectLatest { result ->
                if (result.success && result.data != null) {
                    _historyState.value = if (result.data.isNotEmpty()) {
                        VitalsHistoryUiState.Data(result.data)
                    } else {
                        VitalsHistoryUiState.Empty
                    }
                } else {
                    _historyState.value = VitalsHistoryUiState.Error(result.message)
                }
            } ?: run {
                _historyState.value = VitalsHistoryUiState.Error("User ID not available for history.")
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
