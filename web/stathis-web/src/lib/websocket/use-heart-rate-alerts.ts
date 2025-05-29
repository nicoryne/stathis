'use client';

import { useState, useEffect, useCallback } from 'react';
import { WebSocketManager } from './websocket-client';
import { HeartRateAlertDTO, getHeartRateAlerts } from '@/services/vitals/api-vitals-client';

/**
 * Custom hook for subscribing to real-time heart rate alerts from the backend
 */
export function useHeartRateAlerts(classroomId: string) {
  const [alerts, setAlerts] = useState<HeartRateAlertDTO[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [subscriptionTopics, setSubscriptionTopics] = useState<string[]>([]);

  // Define message handler for heart rate alert updates
  const handleHeartRateAlertMessage = useCallback((data: any) => {
    console.log('Processing heart rate alert:', data);
    
    // Validate message structure
    if (!data || typeof data !== 'object') {
      console.warn('Received invalid heart rate alert format');
      return;
    }
    
    // Validate required fields
    if (!data.studentId || !data.currentHeartRate || !data.thresholdHeartRate) {
      console.warn('Heart rate alert missing required fields:', data);
      return;
    }
    
    // Format the data according to the DTO structure
    const alertData: HeartRateAlertDTO = {
      studentId: data.studentId,
      studentName: data.studentName || 'Unknown Student',
      currentHeartRate: data.currentHeartRate,
      thresholdHeartRate: data.thresholdHeartRate,
      alertMessage: data.alertMessage || `Heart rate exceeded threshold: ${data.currentHeartRate} > ${data.thresholdHeartRate}`,
      timestamp: data.timestamp || new Date().toISOString()
    };
    
    // Add to alerts list (avoiding duplicates)
    setAlerts(prevAlerts => {
      // Check if this alert already exists (same student and timestamp)
      const exists = prevAlerts.some(
        alert => alert.studentId === alertData.studentId && alert.timestamp === alertData.timestamp
      );
      
      if (exists) {
        return prevAlerts;
      }
      
      // Add new alert at the beginning of the array (newest first)
      return [alertData, ...prevAlerts];
    });
    
    setLastUpdated(new Date());
  }, []);
  
  useEffect(() => {
    if (!classroomId) {
      console.log('Missing classroomId, not subscribing to heart rate alerts');
      return;
    }

    console.log('Setting up heart rate alerts subscriptions for classroom:', classroomId);
    const wsManager = WebSocketManager.getInstance();
    const subscriptions: (() => void)[] = [];
    const topics: string[] = [];
    
    // Track connection status
    subscriptions.push(wsManager.subscribe('$SYSTEM/connected', () => {
      setIsConnected(true);
      setError(null);
      console.log('WebSocket connected - ready to receive heart rate alerts');
      
      // Initial data fetch when connected
      refreshData();
    }));
    
    subscriptions.push(wsManager.subscribe('$SYSTEM/disconnected', () => {
      setIsConnected(false);
      setError('Connection lost. Attempting to reconnect...');
    }));

    // Subscribe to all possible topics where heart rate alerts might be published
    
    // 1. Direct classroom alerts topic
    const alertsTopic = `/topic/classroom/${classroomId}/alerts`;
    subscriptions.push(wsManager.subscribe(alertsTopic, handleHeartRateAlertMessage));
    topics.push(alertsTopic);
    
    // 2. Global alerts topic
    const globalAlertsTopic = '/topic/alerts';
    subscriptions.push(wsManager.subscribe(globalAlertsTopic, handleHeartRateAlertMessage));
    topics.push(globalAlertsTopic);
    
    // 3. Wildcard alerts (to catch any alerts from any classroom)
    const wildcardAlertsTopic = '/topic/classroom/+/alerts';
    subscriptions.push(wsManager.subscribe(wildcardAlertsTopic, handleHeartRateAlertMessage));
    topics.push(wildcardAlertsTopic);
    
    // Save subscribed topics for debugging
    setSubscriptionTopics(topics);
    
    // Start connection if not already connected
    if (!wsManager.isConnected()) {
      // Get token from localStorage
      const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;
      wsManager.connect(token || undefined);
    } else {
      setIsConnected(true);
      // Initial data fetch if already connected
      refreshData();
    }

    // Cleanup all subscriptions
    return () => {
      subscriptions.forEach(unsub => unsub());
    };
  }, [classroomId, handleHeartRateAlertMessage]);

  // Method to refresh the connection by reconnecting the WebSocket
  const refreshData = () => {
    if (!classroomId) {
      console.log('Cannot refresh data: missing classroomId');
      return;
    }
    
    console.log('Manually refreshing WebSocket connection for heart rate alerts...');
    
    try {
      // Get the WebSocket manager instance
      const wsManager = WebSocketManager.getInstance();
      
      // Use the reconnect method to establish a fresh connection
      wsManager.reconnect();
      
      // Update last refreshed timestamp
      setLastUpdated(new Date());
      
      // Provide user feedback
      console.log('WebSocket connection refreshed successfully');
    } catch (err) {
      console.error('Error refreshing WebSocket connection:', err);
      setError('Failed to refresh connection');
    }
  };
  
  // Method to clear a specific alert
  const clearAlert = (alertId: string) => {
    setAlerts(prevAlerts => prevAlerts.filter(alert => 
      alert.studentId + alert.timestamp !== alertId
    ));
  };
  
  // Method to clear all alerts
  const clearAllAlerts = () => {
    setAlerts([]);
  };

  return {
    alerts,
    isConnected,
    error,
    lastUpdated,
    refreshData,
    clearAlert,
    clearAllAlerts,
    subscriptionTopics // For debugging purposes
  };
}
