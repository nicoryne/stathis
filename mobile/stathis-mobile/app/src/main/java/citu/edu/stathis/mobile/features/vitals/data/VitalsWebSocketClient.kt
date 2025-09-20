package citu.edu.stathis.mobile.features.vitals.data

import android.util.Log
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsWebSocketDTO
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VitalsWebSocketClient @Inject constructor(
    private val authTokenManager: AuthTokenManager
) {
    private val TAG = "VitalsWebSocketClient"
    
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)  // No timeout for WebSockets
        .build()
        
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _lastSentVitals = MutableStateFlow<VitalsWebSocketDTO?>(null)
    val lastSentVitals: StateFlow<VitalsWebSocketDTO?> = _lastSentVitals.asStateFlow()
    
    private var monitoringJob: Job? = null
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    
    private var currentClassroomId: String? = null
    private var currentTaskId: String? = null
    private var isPreActivity: Boolean = true
    private var isPostActivity: Boolean = false
    
    enum class ConnectionState {
        CONNECTED,
        DISCONNECTED,
        CONNECTING,
        ERROR
    }
    
    // Keeping track of the connection coroutine scope
    private val connectionScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Connect to the WebSocket server
     */
    fun connect(serverUrl: String = BASE_WS_URL) {
        // Don't try to connect if already connecting or connected
        if (_connectionState.value == ConnectionState.CONNECTING || 
            _connectionState.value == ConnectionState.CONNECTED) {
            Timber.d("Already connected or connecting to WebSocket")
            return
        }
        
        // Update state to connecting
        _connectionState.value = ConnectionState.CONNECTING
        
        // Launch connection in background
        connectionScope.launch {
            connectInternal(serverUrl)
        }
    }
    
    /**
     * Internal connection function that handles token retrieval
     */
    private suspend fun connectInternal(serverUrl: String) {
        try {
            // Get token from auth manager
            val token = authTokenManager.accessTokenFlow.firstOrNull()
            
            // Check if we have a valid token
            if (token.isNullOrEmpty()) {
                Timber.e("Cannot connect to WebSocket - No auth token available")
                _connectionState.value = ConnectionState.ERROR
                return
            }
            
            // Connect with the token
            connectWithToken(token, serverUrl)
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving auth token")
            _connectionState.value = ConnectionState.ERROR
        }
    }
    
    private fun connectWithToken(token: String, serverUrl: String) {
        try {
            // Create the WebSocket request with auth token
            val request = Request.Builder()
                .url(serverUrl)
                .addHeader("Authorization", "Bearer $token")
                .build()
            
            // Close any existing connection
            webSocket?.close(1000, "Normal closure")
            webSocket = null
            
            // Create a new WebSocket connection
            webSocket = client.newWebSocket(request, createWebSocketListener())
            
            // Start heartbeat to keep connection alive
            startHeartbeat()
            
            Timber.d("WebSocket connect request sent with token")
        } catch (e: Exception) {
            Timber.e(e, "Error connecting to WebSocket")
            _connectionState.value = ConnectionState.ERROR
            scheduleReconnect()
        }
    }
    
    fun disconnect() {
        monitoringJob?.cancel()
        monitoringJob = null
        
        heartbeatJob?.cancel()
        heartbeatJob = null
        
        reconnectJob?.cancel()
        reconnectJob = null
        
        webSocket?.close(1000, "User initiated disconnect")
        webSocket = null
        
        _connectionState.value = ConnectionState.DISCONNECTED
        Timber.d("WebSocket disconnected")
    }
    
    fun setExerciseContext(classroomId: String, taskId: String, isPreActivity: Boolean = false, isPostActivity: Boolean = false) {
        this.currentClassroomId = classroomId
        this.currentTaskId = taskId
        this.isPreActivity = isPreActivity
        this.isPostActivity = isPostActivity
        Timber.d("Exercise context set: classroomId=$classroomId, taskId=$taskId, pre=$isPreActivity, post=$isPostActivity")
    }
    
    fun startMonitoring(healthConnectManager: HealthConnectManager, intervalMs: Long = 5000) {
        monitoringJob?.cancel()
        
        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    // Fetch current vitals from health connect
                    healthConnectManager.fetchLatestVitals(healthConnectManager.getUserId())
                    // Get vitals from the flow
                    val vitals = healthConnectManager.vitalSigns.value
                    // Send vitals data via WebSocket
                    if (vitals != null) {
                        sendVitalsData(vitals)
                    } else {
                        Timber.d("No vitals data available to send")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in vitals monitoring loop")
                }
                
                delay(intervalMs)
            }
        }
        
        Timber.d("Started vitals monitoring with interval $intervalMs ms")
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        Timber.d("Stopped vitals monitoring")
    }
    
    private fun sendVitalsData(vitals: VitalSigns) {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            Timber.d("Cannot send vitals - WebSocket not connected")
            return
        }
        
        if (currentClassroomId == null || currentTaskId == null) {
            Timber.e("Cannot send vitals - No classroom or task ID set")
            return
        }
        
        try {
            val dto = VitalsWebSocketDTO(
                physicalId = vitals.userId,
                studentId = vitals.userId,
                classroomId = currentClassroomId!!,
                taskId = currentTaskId!!,
                heartRate = vitals.heartRate,
                oxygenSaturation = vitals.oxygenSaturation.toInt(),
                timestamp = LocalDateTime.now(),
                isPreActivity = isPreActivity,
                isPostActivity = isPostActivity
            )
            
            val json = gson.toJson(dto)
            webSocket?.send(json)
            _lastSentVitals.value = dto
            Timber.d("Sent vitals data: $json")
        } catch (e: Exception) {
            Timber.e(e, "Error sending vitals data")
        }
    }
    
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
                Timber.d("WebSocket connection established")
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Timber.d("Received WebSocket message: $text")
                // Handle server messages if needed
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
                Timber.d("WebSocket closed: $code - $reason")
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.ERROR
                Timber.e(t, "WebSocket failure: ${response?.message ?: "Unknown error"}")
                scheduleReconnect()
            }
        }
    }
    
    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(RECONNECT_DELAY_MS)
            if (_connectionState.value != ConnectionState.CONNECTED) {
                Timber.d("Attempting to reconnect to WebSocket")
                connect()
            }
        }
    }
    
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (_connectionState.value == ConnectionState.CONNECTED) {
                    try {
                        webSocket?.send("ping")
                    } catch (e: Exception) {
                        Timber.e(e, "Error sending heartbeat ping")
                    }
                }
            }
        }
    }
    
    companion object {
        private const val BASE_WS_URL = "ws://api.stathis.edu.cit/ws"
        private const val RECONNECT_DELAY_MS = 5000L
        private const val HEARTBEAT_INTERVAL_MS = 30000L
    }
}
