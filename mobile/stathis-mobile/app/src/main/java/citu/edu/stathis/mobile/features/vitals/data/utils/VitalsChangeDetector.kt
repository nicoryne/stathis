package citu.edu.stathis.mobile.features.vitals.data.utils

import android.util.Log
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import kotlin.math.abs

/**
 * Utility class to detect significant changes in vital signs
 * Helps minimize unnecessary API calls by only posting when there are meaningful changes
 */
class VitalsChangeDetector {
    private val TAG = "VitalsChangeDetector"
    
    private var lastPostedVitals: VitalSigns? = null
    private var forceNextPost = false
    
    // Thresholds for significant changes
    private val heartRateThreshold = 5 // beats per minute
    private val oxygenSaturationThreshold = 2 // percentage points
    private val bloodPressureThreshold = 5 // mmHg
    private val temperatureThreshold = 0.2f // degrees Celsius
    private val respirationRateThreshold = 2 // breaths per minute
    
    /**
     * Checks if there are significant changes in vital signs compared to last posted data
     * @param currentVitals The current vital signs to check
     * @return true if there are significant changes, false otherwise
     */
    fun hasSignificantChange(currentVitals: VitalSigns): Boolean {
        // Force post if requested
        if (forceNextPost) {
            Log.d(TAG, "Force posting requested")
            forceNextPost = false
            return true
        }
        
        val lastPosted = lastPostedVitals ?: return true // First time posting
        
        // Check each vital sign for significant changes
        val heartRateChanged = abs(currentVitals.heartRate - lastPosted.heartRate) >= heartRateThreshold
        val oxygenChanged = abs(currentVitals.oxygenSaturation - lastPosted.oxygenSaturation) >= oxygenSaturationThreshold
        val systolicBPChanged = abs(currentVitals.systolicBP - lastPosted.systolicBP) >= bloodPressureThreshold
        val diastolicBPChanged = abs(currentVitals.diastolicBP - lastPosted.diastolicBP) >= bloodPressureThreshold
        val temperatureChanged = abs(currentVitals.temperature - lastPosted.temperature) >= temperatureThreshold
        val respirationChanged = abs(currentVitals.respirationRate - lastPosted.respirationRate) >= respirationRateThreshold
        
        val hasChanges = heartRateChanged || oxygenChanged || systolicBPChanged || 
                        diastolicBPChanged || temperatureChanged || respirationChanged
        
        if (hasChanges) {
            Log.d(TAG, "Significant changes detected: HR=${heartRateChanged}, O2=${oxygenChanged}, " +
                    "BP=${systolicBPChanged || diastolicBPChanged}, Temp=${temperatureChanged}, Resp=${respirationChanged}")
        } else {
            Log.d(TAG, "No significant changes detected")
        }
        
        return hasChanges
    }
    
    /**
     * Updates the last posted vital signs for future change detection
     * @param vitals The vital signs that were successfully posted
     */
    fun updateLastPostedVitals(vitals: VitalSigns) {
        lastPostedVitals = vitals.copy()
        Log.d(TAG, "Updated last posted vitals")
    }
    
    /**
     * Forces the next post to be sent regardless of change detection
     */
    fun forceNextPost() {
        forceNextPost = true
        Log.d(TAG, "Force next post enabled")
    }
    
    /**
     * Resets the change detector (useful when starting a new exercise session)
     */
    fun reset() {
        lastPostedVitals = null
        forceNextPost = false
        Log.d(TAG, "Change detector reset")
    }
    
    /**
     * Resets for a new session (clears last posted vitals but keeps force flag)
     */
    fun resetForNewSession() {
        lastPostedVitals = null
        Log.d(TAG, "Change detector reset for new session")
    }
}
