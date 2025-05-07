package citu.edu.stathis.mobile.features.vitals.data.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import citu.edu.stathis.mobile.features.vitals.domain.model.VitalSigns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "BleManager"

    // Connection parameters - these are critical for BLE stability
    private val CONNECTION_PRIORITY = BluetoothGatt.CONNECTION_PRIORITY_HIGH
    private val GATT_MAX_MTU_SIZE = 517 // Maximum supported MTU size

    // Bluetooth objects
    private val bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    // States
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices.asStateFlow()

    private val _vitalSigns = MutableStateFlow<VitalSigns?>(null)
    val vitalSigns: StateFlow<VitalSigns?> = _vitalSigns.asStateFlow()

    // Scan related
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 15000 // 15 seconds

    private var currentUserId: String? = null
    private var lastConnectedDeviceAddress: String? = null

    enum class ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING
    }

    data class ScannedDevice(
        val device: BluetoothDevice,
        val rssi: Int,
        val scanRecord: ByteArray? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ScannedDevice) return false
            return device.address == other.device.address
        }

        override fun hashCode(): Int {
            return device.address.hashCode()
        }
    }

    // Scan callback with improved error handling
    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val rssi = result.rssi
            val scanRecord = result.scanRecord?.bytes

            val scannedDevice = ScannedDevice(device, rssi, scanRecord)

            _scannedDevices.value = _scannedDevices.value.toMutableList().apply {
                if (!any { it.device.address == device.address }) {
                    add(scannedDevice)
                }
            }.sortedByDescending { it.rssi } // Sort by signal strength

            Log.d(TAG, "Found device: ${device.name ?: "Unknown"} - ${device.address}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
            scanning = false
        }
    }

    // GATT callback with improved error handling and reconnection logic
    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.i(TAG, "Connected to GATT server. Device: $deviceAddress")
                        _connectionState.value = ConnectionState.CONNECTED
                        lastConnectedDeviceAddress = deviceAddress

                        // Request higher connection priority for better data rates
                        gatt.requestConnectionPriority(CONNECTION_PRIORITY)

                        // After connecting, request MTU for better data transfer
                        gatt.requestMtu(GATT_MAX_MTU_SIZE)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.i(TAG, "Disconnected from GATT server. Device: $deviceAddress")
                        _connectionState.value = ConnectionState.DISCONNECTED
                        close()
                    }
                }
            } else {
                // If there's a failure, disconnect and clean up
                Log.w(TAG, "Connection state change with status: $status. Device: $deviceAddress")
                _connectionState.value = ConnectionState.DISCONNECTED
                close()
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered successfully")

                // After services are discovered, simulate vital signs data
                // In a real app, you would read actual values from the device
                simulateVitalSigns()

                // Print all available services for debugging
                gatt.services?.forEach { service ->
                    Log.d(TAG, "Service found: ${service.uuid}")
                    service.characteristics.forEach { characteristic ->
                        Log.d(TAG, "  Characteristic: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(TAG, "MTU changed to: $mtu, status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // After MTU negotiation, discover services
                gatt.discoverServices()
            } else {
                // If MTU negotiation fails, still attempt to discover services
                gatt.discoverServices()
            }
        }

        // For newer Android versions
        @SuppressLint("MissingPermission")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Log.d(TAG, "Characteristic changed: ${characteristic.uuid}, value: ${bytesToHex(value)}")

            // In a real implementation, you would parse the characteristic's value
            // For now, we're using simulated values
        }

        // For older Android versions (pre-Android 12)
        @SuppressLint("MissingPermission")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value
            Log.d(TAG, "Characteristic changed (legacy): ${characteristic.uuid}, value: ${bytesToHex(value)}")

            // In a real implementation, you would parse the characteristic's value
            // For now, we're using simulated values
        }
    }

    // Helper function to convert byte array to hex string for debugging
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF".toCharArray()
        val hexString = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val value = byte.toInt() and 0xFF
            hexString.append(hexChars[value ushr 4])
            hexString.append(hexChars[value and 0x0F])
            hexString.append(' ')
        }
        return hexString.toString()
    }

    // Sets the user ID for the current session
    fun setUserId(userId: String) {
        currentUserId = userId
    }

    // Starts scanning for BLE devices
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasRequiredPermissions()) {
            Log.e(TAG, "Missing required BLE permissions")
            return
        }

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth adapter is null")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }

        if (scanning) {
            Log.d(TAG, "Already scanning")
            return
        }

        // Clear previous scan results
        _scannedDevices.value = emptyList()

        // Using minimal filtering to increase chances of finding the device
        // Real-world applications might use more specific filters
        val filters = ArrayList<ScanFilter>()

        // Using high power scanning for better discovery
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        // Start scanning
        bluetoothAdapter.bluetoothLeScanner?.let { scanner ->
            try {
                scanner.startScan(filters, settings, scanCallback)
                scanning = true
                Log.d(TAG, "Started BLE scan")

                // Stop scanning after a predefined period
                handler.postDelayed({
                    stopScan()
                }, SCAN_PERIOD)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting scan", e)
            }
        } ?: run {
            Log.e(TAG, "Bluetooth LE Scanner is null")
        }
    }

    // Stops scanning for BLE devices
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (scanning && bluetoothAdapter?.bluetoothLeScanner != null) {
            try {
                bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
                scanning = false
                Log.d(TAG, "Stopped BLE scan")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping scan", e)
            }
        }
    }

    // Connects to a BLE device
    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        if (!hasRequiredPermissions()) {
            Log.e(TAG, "Missing required BLE permissions")
            return
        }

        // Disconnect from any existing connection first
        bluetoothGatt?.let {
            it.disconnect()
            it.close()
            bluetoothGatt = null
        }

        _connectionState.value = ConnectionState.CONNECTING

        // Connect with autoConnect=false for immediate connection attempt
        // Use TRANSPORT_LE to ensure BLE connection
        bluetoothGatt =
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)

        Log.d(TAG, "Attempting to connect to device: ${device.address}")
    }

    // Disconnects from the connected BLE device
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.let {
            try {
                it.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting", e)
            }
        }
    }

    // Closes and cleans up BLE resources
    @SuppressLint("MissingPermission")
    fun close() {
        bluetoothGatt?.let {
            try {
                it.close()
                bluetoothGatt = null
                Log.d(TAG, "GATT connection closed")
            } catch (e: Exception) {
                Log.e(TAG, "Error closing GATT connection", e)
            }
        }
        _vitalSigns.value = null
    }

    // Checks if the app has the required BLE permissions
    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    // For development/testing: Simulates vital signs data
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun simulateVitalSigns() {
        val userId = currentUserId ?: return

        // Create a simulated vital signs reading
        val vitalSigns = VitalSigns(
            userId = userId,
            systolicBP = (110..140).random(),
            diastolicBP = (70..90).random(),
            heartRate = (60..100).random(),
            respirationRate = (12..20).random(),
            temperature = (36.0f..37.5f).random(),
            oxygenSaturation = (95.0f..99.0f).random(),
            deviceName = bluetoothGatt?.device?.name ?: bluetoothGatt?.device?.address
        )

        _vitalSigns.value = vitalSigns
        Log.d(TAG, "Simulated vital signs: $vitalSigns")
    }

    // Helper function to generate random float in range
    private fun ClosedFloatingPointRange<Float>.random(): Float {
        return start + (endInclusive - start) * Math.random().toFloat()
    }
}