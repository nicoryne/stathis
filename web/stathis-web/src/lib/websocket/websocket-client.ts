'use client';

import { Client, StompSubscription, IFrame, IMessage } from '@stomp/stompjs';
// @ts-ignore - sockjs-client types are not perfect
import SockJS from 'sockjs-client';
import { API_BASE_URL } from '@/lib/api/server-client';

// Constants
const WEBSOCKET_RECONNECT_MAX_ATTEMPTS = 5;
const WEBSOCKET_INITIAL_RECONNECT_DELAY = 2000;

/**
 * WebSocket Manager for handling STOMP over SockJS connections
 * Matches the mobile app's WebSocket implementation using STOMP protocol
 * Receives vitals data from mobile devices via /topic/classroom/{classroomId}/vitals
 */
export class WebSocketManager {
  private static instance: WebSocketManager;
  private client: Client | null = null;
  private subscriptions: Map<string, Set<(message: any) => void>> = new Map();
  private stompSubscriptions: Map<string, StompSubscription> = new Map();
  private connected = false;
  private connecting = false;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = WEBSOCKET_RECONNECT_MAX_ATTEMPTS;
  private reconnectDelay = WEBSOCKET_INITIAL_RECONNECT_DELAY;
  private messageBuffer: { topic: string; data: any }[] = [];
  private authToken: string | null = null;
  private manualDisconnect = false; // Flag to prevent auto-reconnect after manual disconnect

  private constructor() {}

  /**
   * Get singleton instance
   */
  public static getInstance(): WebSocketManager {
    if (!WebSocketManager.instance) {
      WebSocketManager.instance = new WebSocketManager();
    }
    return WebSocketManager.instance;
  }

  /**
   * Initialize the STOMP over SockJS connection (matching mobile app's backend)
   */
  public connect(token?: string): void {
    if (this.connected || this.connecting) return;
    
    this.connecting = true;
    this.authToken = token || null;
    this.manualDisconnect = false; // Clear manual disconnect flag when connecting
    
    try {
      // Create WebSocket URL for STOMP endpoint
      // Convert http://domain/api to http://domain/ws
      let wsUrl = API_BASE_URL;
      if (wsUrl.endsWith('/api')) {
        wsUrl = wsUrl.replace(/api$/, 'ws');
      } else {
        wsUrl = wsUrl.endsWith('/') ? `${wsUrl}ws` : `${wsUrl}/ws`;
      }
      
      console.log(`Connecting to STOMP WebSocket at ${wsUrl}`);
      
      // Create STOMP client with SockJS
      this.client = new Client({
        webSocketFactory: () => new SockJS(wsUrl) as any,
        connectHeaders: token ? {
          'Authorization': `Bearer ${token}`
        } : {},
        debug: (str) => {
          console.debug('STOMP:', str);
        },
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: this.onConnect.bind(this),
        onStompError: (frame: IFrame) => {
          console.error('STOMP error:', frame);
          this.notifySubscribers('$SYSTEM/error', { frame });
        },
        onWebSocketClose: (event: any) => {
          this.onClose(event);
        },
        onWebSocketError: (event: any) => {
          console.error('WebSocket error:', event);
        },
      });
      
      // Activate the client
      this.client.activate();
      
    } catch (error) {
      console.error('Error connecting to STOMP WebSocket:', error);
      this.connecting = false;
      this.handleReconnect();
    }
  }

  /**
   * Callback when STOMP connection is established
   */
  private onConnect(frame: IFrame): void {
    this.connected = true;
    this.connecting = false;
    this.reconnectAttempts = 0;
    this.reconnectDelay = WEBSOCKET_INITIAL_RECONNECT_DELAY;
    
    console.log('Connected to STOMP WebSocket', frame);
    
    // Re-subscribe to all topics
    this.resubscribeAll();
    
    // Notify subscribers that we're connected
    this.notifySubscribers('$SYSTEM/connected', {});
    
    // Process any messages in the buffer
    this.processMessageBuffer();
  }

  /**
   * Re-subscribe to all previously subscribed topics
   */
  private resubscribeAll(): void {
    if (!this.client || !this.connected) return;
    
    // Clear old STOMP subscriptions
    this.stompSubscriptions.forEach(sub => sub.unsubscribe());
    this.stompSubscriptions.clear();
    
    // Re-subscribe to each topic
    this.subscriptions.forEach((callbacks, topic) => {
      // Skip system topics
      if (topic.startsWith('$SYSTEM/')) return;
      
      this.subscribeToTopic(topic);
    });
  }

  /**
   * Actually subscribe to a STOMP topic
   */
  private subscribeToTopic(topic: string): void {
    if (!this.client || !this.connected) return;
    
    // Don't subscribe if already subscribed
    if (this.stompSubscriptions.has(topic)) return;
    
    console.log(`Subscribing to STOMP topic: ${topic}`);
    
    const subscription = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body);
        console.log(`Received message on topic ${topic}:`, data);
        this.notifySubscribers(topic, data);
      } catch (error) {
        console.error(`Error parsing message from ${topic}:`, error);
      }
    });
    
    this.stompSubscriptions.set(topic, subscription);
  }

  /**
   * Subscribe to a specific topic
   */
  public subscribe(topic: string, callback: (data: any) => void): () => void {
    // Add to our local subscriptions map
    if (!this.subscriptions.has(topic)) {
      this.subscriptions.set(topic, new Set());
      
      // Subscribe to the STOMP topic if connected and not a system topic
      if (this.connected && !topic.startsWith('$SYSTEM/')) {
        this.subscribeToTopic(topic);
      }
    }
    
    const subscribers = this.subscriptions.get(topic)!;
    subscribers.add(callback);
    
    // Return unsubscribe function
    return () => {
      const subscribers = this.subscriptions.get(topic);
      if (subscribers) {
        subscribers.delete(callback);
        if (subscribers.size === 0) {
          this.subscriptions.delete(topic);
          
          // Unsubscribe from STOMP topic if no more subscribers
          const stompSub = this.stompSubscriptions.get(topic);
          if (stompSub) {
            stompSub.unsubscribe();
            this.stompSubscriptions.delete(topic);
          }
        }
      }
    };
  }


  /**
   * Handle WebSocket closure
   */
  private onClose(event: CloseEvent): void {
    console.log(`WebSocket closed: ${event.code} ${event.reason}`);
    this.connected = false;
    this.connecting = false;
    
    // Notify subscribers of disconnection
    this.notifySubscribers('$SYSTEM/disconnected', { 
      code: event.code, 
      reason: event.reason 
    });
    
    // Only attempt reconnection if it was not a normal closure or intentional disconnect
    // Code 1000 = Normal Closure (user initiated)
    // Code 1001 = Going Away (browser navigating away)
    if (event.code !== 1000 && event.code !== 1001) {
      this.handleReconnect();
    } else {
      console.log('WebSocket closed normally, not reconnecting');
    }
  }

  /**
   * Notify subscribers of a message
   */
  private notifySubscribers(topic: string, data: any): void {
    const subscribers = this.subscriptions.get(topic);
    
    if (subscribers && subscribers.size > 0) {
      console.log(`Notifying ${subscribers.size} subscribers for topic: ${topic}`);
      subscribers.forEach(callback => {
        try {
          callback(data);
        } catch (error) {
          console.error(`Error in subscriber callback for ${topic}:`, error);
        }
      });
    }
  }
  
  /**
   * Notify wildcard subscribers when a matching topic receives a message
   * Handles both + (single-level) and # (multi-level) wildcards
   */
  private notifyWildcardSubscribers(topic: string, data: any): void {
    // Check all subscription topics for wildcard patterns
    this.subscriptions.forEach((subscribers, pattern) => {
      if (pattern === topic) return; // Skip exact matches (already handled)
      
      if (this.topicMatchesPattern(topic, pattern)) {
        console.log(`Wildcard match: Topic ${topic} matches pattern ${pattern}`);
        subscribers.forEach(callback => {
          try {
            callback(data);
          } catch (error) {
            console.error(`Error in wildcard subscriber callback for ${pattern}:`, error);
          }
        });
      }
    });
  }
  
  /**
   * Check if a topic matches a pattern with wildcards
   * Handles MQTT-style wildcards: + (single level) and # (multi-level)
   */
  private topicMatchesPattern(topic: string, pattern: string): boolean {
    // Exact match
    if (topic === pattern) return true;
    
    // Convert to arrays for comparison
    const topicSegments = topic.split('/');
    const patternSegments = pattern.split('/');
    
    // Quick length check for optimization
    if (!pattern.includes('#') && topicSegments.length !== patternSegments.length) {
      return false;
    }
    
    // Check each segment
    for (let i = 0; i < patternSegments.length; i++) {
      const patternSeg = patternSegments[i];
      
      // # wildcard matches anything remaining (including zero segments)
      if (patternSeg === '#') {
        return true;
      }
      
      // + wildcard matches exactly one segment
      if (patternSeg === '+') {
        // Make sure we have a segment to match
        if (i >= topicSegments.length) return false;
        continue;
      }
      
      // Exact segment match required
      if (i >= topicSegments.length || patternSeg !== topicSegments[i]) {
        return false;
      }
    }
    
    // Make sure we've matched all segments
    return topicSegments.length === patternSegments.length;
  }

  /**
   * Process any messages in the buffer
   */
  private processMessageBuffer(): void {
    if (!this.connected || this.messageBuffer.length === 0) return;
    
    console.log(`Processing ${this.messageBuffer.length} buffered messages`);
    
    // Process messages with a small delay to avoid overwhelming the socket
    const processNext = () => {
      if (this.messageBuffer.length > 0 && this.connected) {
        const message = this.messageBuffer.shift();
        if (message) {
          this.sendMessage(message.topic, message.data);
          setTimeout(processNext, 50); // Process next message after 50ms
        }
      }
    };
    
    processNext();
  }

  /**
   * Handle reconnection logic with exponential backoff
   */
  private handleReconnect(): void {
    // Don't reconnect if it was a manual disconnect
    if (this.manualDisconnect) {
      console.log('Manual disconnect detected, not reconnecting');
      return;
    }
    
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log(`Maximum reconnect attempts (${this.maxReconnectAttempts}) reached. Giving up.`);
      return;
    }
    
    // Exponential backoff with jitter
    const delay = Math.min(30000, this.reconnectDelay * Math.pow(1.5, this.reconnectAttempts)) + 
                  Math.floor(Math.random() * 1000);
    
    this.reconnectAttempts++;
    console.log(`Attempting to reconnect in ${Math.round(delay / 1000)} seconds (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    setTimeout(() => {
      if (!this.connected && !this.connecting) {
        this.connect();
      }
    }, delay);
  }

  /**
   * Send a message to a specific STOMP destination
   */
  public send(topic: string, data: any): void {
    if (!this.connected || !this.client) {
      // Buffer the message for later
      this.messageBuffer.push({ topic, data });
      return;
    }
    
    try {
      // Send via STOMP protocol
      this.client.publish({
        destination: topic,
        body: JSON.stringify(data),
        headers: { 'content-type': 'application/json' }
      });
      console.log(`Sent message to ${topic}:`, data);
    } catch (error) {
      console.error(`Error sending message to ${topic}:`, error);
      // Add to buffer in case of error
      this.messageBuffer.push({ topic, data });
    }
  }

  /**
   * Send a message to a topic with rate limiting
   */
  public sendMessage(topic: string, data: any): void {
    if (!this.connected) {
      // Buffer the message to send when connected
      this.messageBuffer.push({ topic, data });
      
      if (!this.connecting) {
        this.connect();
      }
      return;
    }
    
    this.send(topic, data);
  }

  /**
   * Disconnect the STOMP WebSocket connection
   */
  public disconnect(): void {
    this.manualDisconnect = true; // Set flag to prevent auto-reconnect
    
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    
    // Clear all STOMP subscriptions
    this.stompSubscriptions.clear();
    
    this.connected = false;
    this.connecting = false;
    console.log('STOMP WebSocket disconnected by user');
  }

  // Function already defined above

  /**
   * Check if WebSocket is connected
   */
  public isConnected(): boolean {
    return this.connected;
  }
  
  /**
   * Reconnect the WebSocket connection
   * This can be used to manually refresh the connection
   */
  public reconnect(): void {
    console.log('Manually reconnecting STOMP WebSocket...');
    
    // Temporarily disable auto-reconnect during manual reconnection
    const wasManualDisconnect = this.manualDisconnect;
    this.manualDisconnect = true;
    
    // Disconnect if currently connected
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.stompSubscriptions.clear();
    }
    
    this.connected = false;
    this.connecting = false;
    
    // Wait a short time to ensure cleanup completes
    setTimeout(() => {
      // Reset reconnection attempts to avoid hitting limits
      this.reconnectAttempts = 0;
      this.reconnectDelay = WEBSOCKET_INITIAL_RECONNECT_DELAY;
      
      // Get token from localStorage if available
      const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;
      
      // Connect with the token (this will clear manualDisconnect flag)
      this.connect(token || undefined);
    }, 500);
  }
  
  /**
   * Get human-readable WebSocket ready state
   */
  private getReadyStateText(): string {
    if (!this.client) return 'CLOSED (No client)';
    
    if (this.connected) return 'CONNECTED';
    if (this.connecting) return 'CONNECTING';
    return 'DISCONNECTED';
  }
}
