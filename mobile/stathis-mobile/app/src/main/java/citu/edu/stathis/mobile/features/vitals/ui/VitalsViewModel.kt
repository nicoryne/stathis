package citu.edu.stathis.mobile.features.vitals.ui

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.vitals.data.ble.BleManager
import citu.edu.stathis.mobile.features.vitals.domain.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.domain.repository.IVitalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VitalsViewModel @Inject constructor(
    private val vitalsRepository: IVitalsRepository,
    private val bleManager: BleManager,
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val TAG = "VitalsViewModel"

    private val _uiState = MutableStateFlow<VitalsUiState>(VitalsUiState.Initial)
    val uiState: StateFlow<VitalsUiState> = _uiState.asStateFlow()

    private val _historyState = MutableStateFlow<VitalsHistoryState>(VitalsHistoryState.Loading)
    val historyState: StateFlow<VitalsHistoryState> = _historyState.asStateFlow()

    private val _deviceState = MutableStateFlow<DeviceState>(DeviceState.Initial)
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()

    private val _bluetoothState = MutableStateFlow(BluetoothState.UNKNOWN)
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            checkBluetoothState()

            val userId = supabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                currentUserId = userId
                bleManager.setUserId(userId)
                loadVitalsHistory()

                // Collect real-time vitals from BLE manager
                bleManager.vitalSigns.collectLatest { vitalSigns ->
                    if (vitalSigns != null) {
                        _uiState.value = VitalsUiState.Data(vitalSigns)
                    }
                }
            } else {
                _uiState.value = VitalsUiState.Error("User not authenticated")
            }
        }

        // Collect connection state changes
        viewModelScope.launch {
            bleManager.connectionState.collectLatest { state ->
                Log.d(TAG, "Connection state changed: $state")
                when (state) {
                    BleManager.ConnectionState.CONNECTED -> {
                        _deviceState.value = DeviceState.Connected(true)
                    }
                    BleManager.ConnectionState.CONNECTING -> {
                        _deviceState.value = DeviceState.Connecting
                    }
                    BleManager.ConnectionState.DISCONNECTED -> {
                        _deviceState.value = DeviceState.Disconnected
                    }
                }
            }
        }

        // Collect scanned devices
        viewModelScope.launch {
            bleManager.scannedDevices.collectLatest { devices ->
                if (_deviceState.value is DeviceState.Scanning) {
                    if (devices.isEmpty()) {
                        // Still scanning - keep the scanning state
                        _deviceState.value = DeviceState.Scanning
                    } else {
                        _deviceState.value = DeviceState.ScannedDevices(devices.map { it.device })
                    }
                }
            }
        }
    }

    private fun checkBluetoothState() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter

        _bluetoothState.value = when {
            bluetoothAdapter == null -> BluetoothState.NOT_SUPPORTED
            !bluetoothAdapter.isEnabled -> BluetoothState.DISABLED
            else -> BluetoothState.ENABLED
        }
    }

    fun startScan() {
        Log.d(TAG, "Starting BLE scan")
        checkBluetoothState()

        if (_bluetoothState.value != BluetoothState.ENABLED) {
            Log.e(TAG, "Bluetooth not enabled")
            return
        }

        _deviceState.value = DeviceState.Scanning
        bleManager.startScan()

        // Add a safety timeout in case scanning gets stuck
        viewModelScope.launch {
            delay(20000) // 20 seconds max scan time
            if (_deviceState.value is DeviceState.Scanning) {
                Log.d(TAG, "Scan timeout - stopping scan")
                stopScan()

                // If no devices found, update state accordingly
                if (bleManager.scannedDevices.value.isEmpty()) {
                    _deviceState.value = DeviceState.NoDevicesFound
                }
            }
        }
    }

    fun stopScan() {
        Log.d(TAG, "Stopping BLE scan")
        bleManager.stopScan()
    }

    fun connectToDevice(device: BluetoothDevice) {
        Log.d(TAG, "Connecting to device: ${device.address}")
        bleManager.connectToDevice(device)
    }

    fun disconnectDevice() {
        Log.d(TAG, "Disconnecting device")
        bleManager.disconnect()
    }

    fun saveCurrentVitals() {
        viewModelScope.launch {
            val vitalSigns = bleManager.vitalSigns.value ?: return@launch
            val userId = currentUserId ?: return@launch

            Log.d(TAG, "Saving vital signs: $vitalSigns")

            vitalsRepository.saveVitalSigns(vitalSigns.copy(userId = userId)).fold(
                onSuccess = {
                    Log.d(TAG, "Vital signs saved successfully")
                    loadVitalsHistory()
                },
                onFailure = { error ->
                    Log.e(TAG, "Error saving vital signs", error)
                    // Handle error
                }
            )
        }
    }

    private fun loadVitalsHistory() {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            _historyState.value = VitalsHistoryState.Loading

            Log.d(TAG, "Loading vitals history for user: $userId")

            try {
                vitalsRepository.getVitalSignsHistory(userId).collectLatest { vitalsList ->
                    _historyState.value = if (vitalsList.isNotEmpty()) {
                        Log.d(TAG, "Loaded ${vitalsList.size} vital records")
                        VitalsHistoryState.Data(vitalsList)
                    } else {
                        Log.d(TAG, "No vital records found")
                        VitalsHistoryState.Empty
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading vitals history", e)
                _historyState.value = VitalsHistoryState.Empty
            }
        }
    }

    fun deleteVitalRecord(id: String) {
        viewModelScope.launch {
            Log.d(TAG, "Deleting vital record: $id")
            vitalsRepository.deleteVitalSigns(id).fold(
                onSuccess = {
                    Log.d(TAG, "Vital record deleted successfully")
                    loadVitalsHistory()
                },
                onFailure = { error ->
                    Log.e(TAG, "Error deleting vital record", error)
                    // Handle error
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleManager.disconnect()
        bleManager.close()
    }
}

sealed class VitalsUiState {
    data object Initial : VitalsUiState()
    data class Data(val vitalSigns: VitalSigns) : VitalsUiState()
    data class Error(val message: String) : VitalsUiState()
}

sealed class VitalsHistoryState {
    data object Loading : VitalsHistoryState()
    data class Data(val vitalsList: List<VitalSigns>) : VitalsHistoryState()
    data object Empty : VitalsHistoryState()
}

sealed class DeviceState {
    data object Initial : DeviceState()
    data object Scanning : DeviceState()
    data class ScannedDevices(val devices: List<BluetoothDevice>) : DeviceState()
    data object Connecting : DeviceState()
    data class Connected(val isConnected: Boolean) : DeviceState()
    data object Disconnected : DeviceState()
    data object NoDevicesFound : DeviceState()
}

enum class BluetoothState {
    UNKNOWN,
    ENABLED,
    DISABLED,
    NOT_SUPPORTED
}