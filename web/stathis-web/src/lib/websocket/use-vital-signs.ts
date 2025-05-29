'use client';

import { useState, useEffect } from 'react';
import { WebSocketManager } from './websocket-client';
import { VitalSignsDTO } from '@/services/vitals/api-vitals-client';
import { getStudentVitalSigns } from '@/services/vitals/api-vitals-client';

/**
 * Custom hook for subscribing to real-time vital signs from the backend
 */
export function useVitalSigns(studentId: string, taskId: string) {
  const [vitalSigns, setVitalSigns] = useState<VitalSignsDTO | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  useEffect(() => {
    if (!studentId || !taskId) return;

    const wsManager = WebSocketManager.getInstance();
    const subscriptions: (() => void)[] = [];
    
    // Track connection status
    subscriptions.push(wsManager.subscribe('$SYSTEM/connected', () => {
      setIsConnected(true);
      setError(null);
      console.log('WebSocket connected - ready to receive vital signs');
    }));
    
    subscriptions.push(wsManager.subscribe('$SYSTEM/disconnected', () => {
      setIsConnected(false);
      setError('Connection lost. Attempting to reconnect...');
    }));

    // Subscribe to the global topic that the backend may use
    subscriptions.push(wsManager.subscribe('/topic/vitals', (data) => {
      // Filter messages for our specific student and task
      if (data.studentId === studentId && data.taskId === taskId) {
        console.log('Received vital signs from /topic/vitals for:', studentId, 'task:', taskId);
        setVitalSigns(data);
        setLastUpdated(new Date());
      }
    }));
    
    // Also subscribe to all possible classroom topics using a "wildcard" approach
    // by creating subscriptions for common patterns
    // First: Try the pattern that we observed in the backend code
    const classroomSubscriber = (data: any) => {
      // Only update if the message is for our student and task
      if (data.studentId === studentId && data.taskId === taskId) {
        console.log('Received vital signs from classroom topic for:', studentId, 'task:', taskId);
        setVitalSigns(data);
        setLastUpdated(new Date());
      }
    };
    
    // Since we don't know which classroom the student is in, subscribe to a broader topic pattern
    // that will catch all classroom-related vital signs
    subscriptions.push(wsManager.subscribe('/topic/classroom/+/vitals', classroomSubscriber));
    
    // If + wildcard is not supported, try with # wildcard which is sometimes used
    subscriptions.push(wsManager.subscribe('/topic/classroom/#', classroomSubscriber));
    
    // As a fallback, subscribe to specific topics if we know common classroom IDs
    // This would need to be expanded with actual classroom IDs
    
    // Start connection if not already connected
    if (!wsManager.isConnected()) {
      // Get token from localStorage
      const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;
      wsManager.connect(token || undefined);
    } else {
      setIsConnected(true);
    }

    // Cleanup all subscriptions
    return () => {
      subscriptions.forEach(unsub => unsub());
    };
  }, [studentId, taskId]);

  // Method to request immediate data refresh through a REST API
  const refreshData = async () => {
    if (!studentId || !taskId) return;
    
    try {
      // Use the existing API client to get the latest vital signs
      const data = await getStudentVitalSigns(taskId, studentId);
      setVitalSigns(data);
      setLastUpdated(new Date());
    } catch (err) {
      console.error('Error refreshing vital signs data:', err);
      setError('Failed to refresh data');
    }
  };

  return {
    vitalSigns,
    isConnected,
    error,
    lastUpdated,
    refreshData
  };
}
