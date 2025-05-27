'use client';

import { useState, useEffect } from 'react';
import { WebSocketManager } from './websocket-client';
import { VitalSignsDTO } from '@/services/vitals/api-vitals-client';

/**
 * Custom hook for subscribing to real-time vital signs with optimized polling
 */
export function useVitalSigns(studentId: string, taskId: string) {
  const [vitalSigns, setVitalSigns] = useState<VitalSignsDTO | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  useEffect(() => {
    if (!studentId || !taskId) return;

    const wsManager = WebSocketManager.getInstance();
    
    // Track connection status
    const connectionSub = wsManager.subscribe('$SYSTEM/connected', () => {
      setIsConnected(true);
      setError(null);
    });
    
    const disconnectionSub = wsManager.subscribe('$SYSTEM/disconnected', () => {
      setIsConnected(false);
      setError('Connection lost. Attempting to reconnect...');
    });

    // Subscribe to vital signs topic for this student and task
    const vitalSignsSub = wsManager.subscribe(`/topic/vitals/${studentId}/${taskId}`, (data) => {
      setVitalSigns(data);
      setLastUpdated(new Date());
    });
    
    // Send request for initial vital signs data
    // This helps reduce load by only requesting data when needed
    wsManager.sendMessage('/app/vitals/request', {
      studentId,
      taskId,
      requestType: 'INITIAL'
    });
    
    // Request updates at a reasonable interval (e.g., every 10 seconds)
    // This is a compromise between real-time updates and avoiding rate limits
    const intervalId = setInterval(() => {
      if (wsManager.isConnected()) {
        wsManager.sendMessage('/app/vitals/request', {
          studentId,
          taskId,
          requestType: 'UPDATE'
        });
      }
    }, 10000); // 10 seconds
    
    // Start connection if not already connected
    if (!wsManager.isConnected()) {
      // Get token from localStorage
      const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;
      wsManager.connect(token || undefined);
    } else {
      setIsConnected(true);
    }

    // Cleanup
    return () => {
      connectionSub();
      disconnectionSub();
      vitalSignsSub();
      clearInterval(intervalId);
    };
  }, [studentId, taskId]);

  // Method to request immediate data refresh
  const refreshData = () => {
    if (!studentId || !taskId) return;
    
    const wsManager = WebSocketManager.getInstance();
    wsManager.sendMessage('/app/vitals/request', {
      studentId,
      taskId,
      requestType: 'IMMEDIATE'
    });
  };

  return {
    vitalSigns,
    isConnected,
    error,
    lastUpdated,
    refreshData
  };
}
