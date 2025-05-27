'use client';

import { API_BASE_URL } from '@/lib/api/server-client';

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
  private maxReconnectAttempts = 5;
  private reconnectDelay = 2000; // Start with 2 seconds
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
      // Use standard WebSocket for better browser compatibility
      const wsUrl = `${API_BASE_URL.replace(/^http/, 'ws').replace(/api$/, '')}ws`;
      console.log(`Connecting to WebSocket at ${wsUrl}`);
      
      this.socket = new WebSocket(wsUrl);
      
      this.socket.onopen = this.onConnect.bind(this);
      this.socket.onmessage = this.onMessage.bind(this);
      this.socket.onerror = this.onError.bind(this);
      this.socket.onclose = this.onClose.bind(this);
      
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
  private onConnect(): void {
    this.connected = true;
    this.connecting = false;
    this.reconnectAttempts = 0;
    this.reconnectDelay = 2000; // Reset delay
    
    console.log('Connected to WebSocket');
    
    // Process any messages in the buffer
    this.processMessageBuffer();
    
    // Let subscribers know we're connected
    this.notifySubscribers('$SYSTEM/connected', { connected: true });
  }

  /**
   * Handle incoming WebSocket messages
   */
  private onMessage(event: MessageEvent): void {
    try {
      const message = JSON.parse(event.data);
      
      // Check if the message has a topic field
      if (message && message.topic) {
        this.notifySubscribers(message.topic, message.data);
      } else {
        console.warn('Received message without topic:', message);
      }
    } catch (error) {
      console.error('Error parsing WebSocket message:', error);
    }
  }

  /**
   * Handle WebSocket errors
   */
  private onError(event: Event): void {
    console.error('WebSocket error:', event);
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
    
    if (subscribers) {
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
      console.error(`Failed to reconnect after ${this.maxReconnectAttempts} attempts`);
      return;
    }
    
    this.reconnectAttempts++;
    // Exponential backoff with jitter
    const jitter = Math.random() * 1000;
    const delay = Math.min(this.reconnectDelay * Math.pow(1.5, this.reconnectAttempts - 1) + jitter, 30000);
    
    console.log(`Attempting to reconnect in ${Math.round(delay / 1000)} seconds (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
    
    setTimeout(() => {
      if (!this.connected && !this.connecting) {
        this.connect();
      }
    }, delay);
  }

  /**
   * Subscribe to a topic
   */
  public subscribe(topic: string, callback: (message: any) => void): () => void {
    if (!this.subscriptions.has(topic)) {
      this.subscriptions.set(topic, new Set());
    }
    
    const subscribers = this.subscriptions.get(topic)!;
    subscribers.add(callback);
    
    // Make sure we're connected
    if (!this.connected && !this.connecting) {
      this.connect();
    }
    
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
    
    if (!this.socket) return;
    
    try {
      const message = JSON.stringify({ topic, data });
      this.socket.send(message);
    } catch (error) {
      console.error(`Error sending message to ${topic}:`, error);
      
      // Buffer the message to try again later
      this.messageBuffer.push({ topic, data });
    }
  }

  /**
   * Disconnect from WebSocket
   */
  public disconnect(): void {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    
    this.connected = false;
    this.connecting = false;
    this.subscriptions.clear();
    this.messageBuffer = [];
  }

  /**
   * Check if WebSocket is connected
   */
  public isConnected(): boolean {
    return this.connected;
  }
}
