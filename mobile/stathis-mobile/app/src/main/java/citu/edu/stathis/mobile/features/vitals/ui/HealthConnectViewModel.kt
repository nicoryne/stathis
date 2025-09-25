package citu.edu.stathis.mobile.features.vitals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import citu.edu.stathis.mobile.features.vitals.data.VitalsCache
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val vitalsCache: VitalsCache
) : ViewModel() {

    private val _connectionState = MutableStateFlow(HealthConnectManager.ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<HealthConnectManager.ConnectionState> = _connectionState.asStateFlow()

    private val _vitalSigns = MutableStateFlow<VitalSigns?>(null)
    val vitalSigns: StateFlow<VitalSigns?> = _vitalSigns.asStateFlow()

    private val _cachedVitals = MutableStateFlow<VitalSigns?>(null)
    val cachedVitals: StateFlow<VitalSigns?> = _cachedVitals.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private var monitoringJob: Job? = null

    init {
        // Observe connection state from HealthConnectManager
        viewModelScope.launch {
            healthConnectManager.connectionState.collect { state ->
                _connectionState.value = state
                
                // Load cached vitals when disconnected
                if (state == HealthConnectManager.ConnectionState.DISCONNECTED) {
                    loadCachedVitals()
                }
            }
        }

        // Observe vitals from HealthConnectManager
        viewModelScope.launch {
            healthConnectManager.vitalSigns.collect { vitals ->
                _vitalSigns.value = vitals
                
                // Cache vitals when we receive new data
                if (vitals != null) {
                    vitalsCache.cacheVitals(vitals)
                }
            }
        }

        // Observe cached vitals from VitalsCache
        viewModelScope.launch {
            vitalsCache.cachedVitals.collect { cached ->
                _cachedVitals.value = cached
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            healthConnectManager.connect()
        }
    }

    fun disconnect() {
        healthConnectManager.disconnect()
        stopMonitoring()
    }

    fun startMonitoring() {
        if (_isMonitoring.value) return
        
        _isMonitoring.value = true
        
        monitoringJob = viewModelScope.launch {
            while (_isMonitoring.value) {
                if (_connectionState.value == HealthConnectManager.ConnectionState.CONNECTED) {
                    healthConnectManager.fetchLatestVitals()
                }
                delay(5000) // Poll every 5 seconds
            }
        }
    }

    fun stopMonitoring() {
        _isMonitoring.value = false
        monitoringJob?.cancel()
        monitoringJob = null
    }

    private fun loadCachedVitals() {
        // Load cached vitals from VitalsCache
        val cached = vitalsCache.getCachedVitals()
        if (cached != null && vitalsCache.isCacheValid()) {
            _cachedVitals.value = cached
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
