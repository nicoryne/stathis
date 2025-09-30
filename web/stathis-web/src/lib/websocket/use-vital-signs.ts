'use client';

import { useState, useEffect, useCallback } from 'react';
import { WebSocketManager } from './websocket-client';
import { VitalSignsDTO, getStudentVitalSigns } from '@/services/vitals/api-vitals-client';

/**
 * Custom hook for subscribing to real-time vital signs from the backend
 */
export function useVitalSigns(studentId: string, taskId: string) {
  const [vitalSigns, setVitalSigns] = useState<VitalSignsDTO | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [subscriptionTopics, setSubscriptionTopics] = useState<string[]>([]);

  // Define message handler for vital sign updates
  const handleVitalSignsMessage = useCallback((data: any) => {
    console.log('Processing potential vital signs data:', data);
    
    // Validate message structure
    if (!data || typeof data !== 'object') {
      console.warn('Received invalid vital signs data format');
      return;
    }
    
    // Extract studentId and taskId from the message
    const messageStudentId = data.studentId;
    const messageTaskId = data.taskId;
    
    // Only process if this message is for our specific student and task
    if (messageStudentId === studentId && messageTaskId === taskId) {
      console.log('✅ Matched vital signs for:', studentId, 'task:', taskId);
      
      // Validate required fields
      if (typeof data.heartRate !== 'number' || typeof data.oxygenSaturation !== 'number') {
        console.warn('Vital signs data missing required fields:', data);
        return;
      }
      
      // Format the data according to the DTO structure
      const vitalSignsData: VitalSignsDTO = {
        physicalId: data.physicalId || undefined,
        studentId: data.studentId,
        classroomId: data.classroomId,
        taskId: data.taskId,
        heartRate: data.heartRate,
        oxygenSaturation: data.oxygenSaturation,
        timestamp: data.timestamp,
        isPreActivity: data.isPreActivity,
        isPostActivity: data.isPostActivity
      };
      
      // Update state with the new data
      setVitalSigns(vitalSignsData);
      setLastUpdated(new Date());
    }
  }, [studentId, taskId]);
  
  useEffect(() => {
    if (!studentId || !taskId) {
      console.log('Missing studentId or taskId, not subscribing to vital signs');
      return;
    }

    console.log('Setting up vital signs subscriptions for student:', studentId, 'task:', taskId);
    const wsManager = WebSocketManager.getInstance();
    const subscriptions: (() => void)[] = [];
    const topics: string[] = [];
    
    // Track connection status
    subscriptions.push(wsManager.subscribe('$SYSTEM/connected', () => {
      setIsConnected(true);
      setError(null);
      console.log('WebSocket connected - ready to receive vital signs');
      
      // Initial data fetch when connected
      refreshData();
    }));
    
    subscriptions.push(wsManager.subscribe('$SYSTEM/disconnected', () => {
      setIsConnected(false);
      setError('Connection lost. Attempting to reconnect...');
    }));

    // Subscribe to all possible topics where vital signs might be published
    
    // 1. Global topic
    const globalTopic = '/topic/vitals';
    subscriptions.push(wsManager.subscribe(globalTopic, handleVitalSignsMessage));
    topics.push(globalTopic);
    
    // 2. Student-specific topic
    const studentTopic = `/topic/student/${studentId}/vitals`;
    subscriptions.push(wsManager.subscribe(studentTopic, handleVitalSignsMessage));
    topics.push(studentTopic);
    
    // 3. Task-specific topic
    const taskTopic = `/topic/task/${taskId}/vitals`;
    subscriptions.push(wsManager.subscribe(taskTopic, handleVitalSignsMessage));
    topics.push(taskTopic);
    
    // 4. Combined student+task topic
    const combinedTopic = `/topic/student/${studentId}/task/${taskId}/vitals`;
    subscriptions.push(wsManager.subscribe(combinedTopic, handleVitalSignsMessage));
    topics.push(combinedTopic);
    
    // 5. Classroom wildcards (based on backend code)
    // Pattern from VitalSignsService: /topic/classroom/{classroomId}/vitals
    const classroomWildcardTopic = '/topic/classroom/+/vitals';
    subscriptions.push(wsManager.subscribe(classroomWildcardTopic, handleVitalSignsMessage));
    topics.push(classroomWildcardTopic);
    
    // 6. Multi-level wildcard for any classroom messages
    const multiLevelWildcardTopic = '/topic/classroom/#';
    subscriptions.push(wsManager.subscribe(multiLevelWildcardTopic, handleVitalSignsMessage));
    topics.push(multiLevelWildcardTopic);
    
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
  }, [studentId, taskId]);

  // Method to refresh the connection by reconnecting the WebSocket
  const refreshData = () => {
    if (!studentId || !taskId) {
      console.log('Cannot refresh data: missing studentId or taskId');
      return;
    }
    
    console.log('Manually refreshing WebSocket connection for vital signs...');
    
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

  return {
    vitalSigns,
    isConnected,
    error,
    lastUpdated,
    refreshData,
    subscriptionTopics // For debugging purposes
  };
}
