'use client';

import { API_BASE_URL } from '@/lib/api/server-client';

// Constants
const WEBSOCKET_RECONNECT_MAX_ATTEMPTS = 5;
const WEBSOCKET_INITIAL_RECONNECT_DELAY = 2000;

/**
 * WebSocket Manager for handling STOMP over SockJS connections
 */
export class WebSocketManager {
  private static instance: WebSocketManager;
  private socket: WebSocket | null = null;
  private subscriptions: Map<string, Set<(message: any) => void>> = new Map();
  private connected = false;
  private connecting = false;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = WEBSOCKET_RECONNECT_MAX_ATTEMPTS;
  private reconnectDelay = WEBSOCKET_INITIAL_RECONNECT_DELAY; // Start with 2 seconds
  private messageBuffer: { topic: string; data: any }[] = [];

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
   * Initialize the WebSocket connection with rate limiting
   */
  public connect(token?: string): void {
    if (this.connected || this.connecting) return;
    
    this.connecting = true;
    
    try {
      // Use standard WebSocket with URL from API_BASE_URL
      // Format: Convert http://domain/api to ws://domain/ws
      let wsUrl = API_BASE_URL.replace(/^http/, 'ws');
      
      // Remove 'api' from the end if present and add 'ws'
      if (wsUrl.endsWith('/api')) {
        wsUrl = wsUrl.replace(/api$/, 'ws');
      } else if (!wsUrl.endsWith('/ws')) {
        // If no 'api' at the end, ensure it ends with '/ws'
        wsUrl = wsUrl.endsWith('/') ? `${wsUrl}ws` : `${wsUrl}/ws`;
      }
      
      console.log(`Connecting to WebSocket at ${wsUrl}`);
      
      this.socket = new WebSocket(wsUrl);
      
      this.socket.onopen = this.onConnect.bind(this);
      this.socket.onmessage = this.onMessage.bind(this);
      this.socket.onerror = this.onError.bind(this);
      this.socket.onclose = this.onClose.bind(this);
      
      // Log connection status changes
      console.log(`WebSocket connection state: ${this.getReadyStateText()}`);
      
      // Add authentication if provided
      if (token) {
        // For custom authentication headers, you might need to use a handshake mechanism
        // or query parameters depending on your server configuration
        this.sendMessage('AUTH', { token });
      }
    } catch (error) {
      console.error('Error connecting to WebSocket:', error);
      this.connecting = false;
      this.handleReconnect();
    }
  }

  /**
   * Callback when connection is established
   */
  private onConnect(event: Event): void {
    this.connected = true;
    this.connecting = false;
    this.reconnectAttempts = 0;
    this.reconnectDelay = 2000; // Reset delay
    
    console.log('Connected to WebSocket');
    
    // Notify subscribers that we're connected
    this.notifySubscribers('$SYSTEM/connected', {});
    
    // Process any messages in the buffer
    this.processMessageBuffer();
  }

  /**
   * Subscribe to a specific topic
   */
  public subscribe(topic: string, callback: (data: any) => void): () => void {
    // Add to our local subscriptions map
    if (!this.subscriptions.has(topic)) {
      this.subscriptions.set(topic, new Set());
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
        }
      }
    };
  }

  /**
   * Handle incoming WebSocket messages
   */
  private onMessage(event: MessageEvent): void {
    try {
      // Log raw message for debugging
      console.debug('Raw WebSocket message received:', event.data);
      
      const data = JSON.parse(event.data);
      
      // Spring STOMP messages have a structure we need to parse
      if (data && typeof data === 'object') {
        // Extract topic and payload from the message
        // This handles both our custom format and potential STOMP messages
        const topic = data.topic || data.destination || '';
        let payload: any;
        
        try {
          // Handle different message formats
          if (data.body && typeof data.body === 'string') {
            payload = JSON.parse(data.body);
          } else {
            payload = data.data || data;
          }
          
          console.log(`WebSocket message received on topic: ${topic}`, payload);
          
          if (topic) {
            // Notify direct topic subscribers
            this.notifySubscribers(topic, payload);
            
            // Also notify any wildcard subscribers that match this topic
            this.notifyWildcardSubscribers(topic, payload);
          } else {
            console.warn('WebSocket message received without topic');
            // Broadcast to all subscribers if no topic is specified
            this.subscriptions.forEach((subscribers, registeredTopic) => {
              this.notifySubscribers(registeredTopic, payload as any);
            });
          }
        } catch (parseError) {
          console.error('Error parsing message body:', parseError, data);
          // Try to notify with the raw data as fallback
          if (topic) {
            this.notifySubscribers(topic, data);
          }
        }
      }
    } catch (error) {
      console.error('Error parsing WebSocket message:', error, event.data);
    }
  }

  /**
   * Handle WebSocket errors
   */
  private onError(event: Event): void {
    const errorDetails = {
      timestamp: new Date().toISOString(),
      connectionState: this.connected ? 'connected' : 'disconnected',
      reconnectAttempts: this.reconnectAttempts,
      eventType: 'error'
    };
    
    console.error('WebSocket error:', errorDetails);
    
    // Notify subscribers of the error
    this.notifySubscribers('$SYSTEM/error', errorDetails);
    
    // Wait for the close event which will trigger reconnection
    console.warn('WebSocket encountered an error. Waiting for connection to close before reconnecting.');
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
    
    this.handleReconnect();
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
   * Send a message to a specific topic
   */
  public send(topic: string, data: any): void {
    if (!this.connected || !this.socket) {
      // Buffer the message for later
      this.messageBuffer.push({ topic, data });
      return;
    }
    
    try {
      // Format message in a way that Spring's STOMP handlers can understand
      const message = JSON.stringify({
        destination: topic,
        body: JSON.stringify(data),
        headers: { 'content-type': 'application/json' }
      });
      
      this.socket.send(message);
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
   * Disconnect the WebSocket connection
   */
  public disconnect(): void {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    
    this.connected = false;
    this.connecting = false;
    console.log('WebSocket disconnected by user');
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
    console.log('Manually reconnecting WebSocket...');
    
    // Disconnect if currently connected
    if (this.socket) {
      this.disconnect();
    }
    
    // Wait a short time to ensure cleanup completes
    setTimeout(() => {
      // Reset reconnection attempts to avoid hitting limits
      this.reconnectAttempts = 0;
      this.reconnectDelay = WEBSOCKET_INITIAL_RECONNECT_DELAY;
      
      // Get token from localStorage if available
      const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;
      
      // Connect with the token
      this.connect(token || undefined);
    }, 500);
  }
  
  /**
   * Get human-readable WebSocket ready state
   */
  private getReadyStateText(): string {
    if (!this.socket) return 'CLOSED (No socket)';
    
    switch (this.socket.readyState) {
      case WebSocket.CONNECTING: return 'CONNECTING';
      case WebSocket.OPEN: return 'OPEN';
      case WebSocket.CLOSING: return 'CLOSING';
      case WebSocket.CLOSED: return 'CLOSED';
      default: return `UNKNOWN (${this.socket.readyState})`;
    }
  }
}
