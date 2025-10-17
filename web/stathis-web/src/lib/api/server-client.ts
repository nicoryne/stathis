'use client';

import { handleApiError } from './error-interceptor';

/**
 * API client for backend requests
 */
export const API_BASE_URL = 'https://api-stathis.ryne.dev/api';

export interface ApiResponse<T = any> {
  data?: T;
  error?: string;
  status: number;
}

/**
 * Generic fetch wrapper for server components
 */
async function fetchApi<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  try {
    const url = `${API_BASE_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;
    
    console.log(`[API Request] ${options.method || 'GET'} ${url}`, { body: options.body });
    
    // Default headers
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string> || {})
    };
    
    // Check if this is a public auth endpoint that doesn't need authentication
    const isAuthEndpoint = endpoint.includes('/auth/');
    const isPublicAuthEndpoint = isAuthEndpoint && 
      (endpoint.includes('/register') || 
       endpoint.includes('/login') || 
       endpoint.includes('/forgot-password') || 
       endpoint.includes('/reset-password'));
    
    // Get auth and refresh tokens
    let authToken: string | null = null;
    let refreshToken: string | null = null;
    
    if (typeof window !== 'undefined') {
      // Client-side: get tokens from localStorage
      authToken = localStorage.getItem('auth_token');
      refreshToken = localStorage.getItem('auth_token_refresh');
    } else {
      // Server-side: We can't access localStorage, but this shouldn't execute client-side
      console.warn('Running server-side without token access');
    }
    
    // Get auth token from options headers if provided (allows for token override)
    const headerAuthToken = options.headers && 
      typeof options.headers === 'object' && 
      'Authorization' in options.headers ? 
      (options.headers as any).Authorization : null;
      
    if (headerAuthToken && typeof headerAuthToken === 'string') {
      // If Authorization header is already set in options, use that
      headers['Authorization'] = headerAuthToken;
      console.log('Using Authorization header from options');
    }
    // Only add auth token for endpoints that need authentication
    else if (authToken && !isPublicAuthEndpoint) {
      // Format and set authorization header
      const formattedToken = authToken.trim();
      headers['Authorization'] = formattedToken.startsWith('Bearer ') ? 
        formattedToken : `Bearer ${formattedToken}`;
      
      // Log detailed info for debugging (without sensitive data)
      console.log('🔐 Auth Details', { 
        tokenType: 'Bearer', 
        tokenLength: authToken.length,
        endpoint,
        headersPresent: Object.keys(headers),
        method: options.method || 'GET'
      });
      
      // Check if token might be malformed
      if (!authToken.includes('.') || authToken.split('.').length !== 3) {
        console.warn('⚠️ Token appears to be malformed - not in JWT format');
      }
      
      try {
        // Try to decode the token to check expiration
        const tokenParts = authToken.split('.');
        if (tokenParts.length === 3) {
          const payload = JSON.parse(atob(tokenParts[1].replace(/-/g, '+').replace(/_/g, '/')));
          
          // Check if token is expired or about to expire
          const expirationTime = payload.exp * 1000; // Convert to milliseconds
          const currentTime = Date.now();
          const timeUntilExpiry = expirationTime - currentTime;
          
          // If token expires in less than 5 minutes, attempt to refresh
          if (timeUntilExpiry < 5 * 60 * 1000 && timeUntilExpiry > 0 && refreshToken) {
            console.log('Token expires soon, should refresh');
            // For now, we'll just continue with the existing token
            // In a real implementation, we would refresh the token here
          } else if (timeUntilExpiry <= 0) {
            console.warn('⚠️ Token is expired');
          }
        }
      } catch (e) {
        console.error('Error decoding token:', e);
      }
    } else if (!authToken && !isPublicAuthEndpoint) {
      console.warn('No auth token found for authenticated request to:', endpoint);
    }
    
    // Include refresh token in headers if available (but not for public auth endpoints)
    if (refreshToken && !isPublicAuthEndpoint) {
      headers['X-Refresh-Token'] = refreshToken;
    }
    
    // Add to URL params for endpoints that expect it as a parameter
    // ONLY do this for specific auth endpoints
    if (isAuthEndpoint && (endpoint.includes('/refresh') || endpoint.includes('/token'))) {
      if (endpoint.includes('?')) {
        endpoint += `&refreshToken=${refreshToken || ''}`;
      } else {
        endpoint += `?refreshToken=${refreshToken || ''}`;
      }
    }

    // Log actual request configuration that will be sent
    console.log('[API] Sending request to:', url);
    console.log('[API] Request headers:', headers);
    console.log('[API] Request options:', {
      method: options.method,
      // credentials removed
      mode: 'cors',
      body: options.body
    });
    
    let response;
    try {
      // Log the actual request being sent
      console.log('[API] Sending request with headers:', { 
        url,
        method: options.method || 'GET',
        authPresent: !!headers['Authorization']
      });
      
      // Send the request with authentication headers
      response = await fetch(url, {
        ...options,
        headers,
        credentials: 'same-origin', // Use same-origin to allow cookies but prevent CORS issues
        mode: 'cors'                // Explicitly use CORS mode
      });

      console.log(`[API Response] Status: ${response.status}`);
      console.log('[API Response] Headers:', Object.fromEntries([...response.headers.entries()]));
    } catch (networkError) {
      console.error('[API] Network error during fetch:', networkError);
      return {
        error: networkError instanceof Error ? networkError.message : 'Network error during fetch',
        status: 0 // 0 indicates a network error
      };
    }

    // Try to parse JSON response
    let data;
    try {
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      console.log('[API Response Data]', data);
    } catch (parseError) {
      console.error('[API] Error parsing response:', parseError);
      return {
        error: 'Failed to parse response',
        status: response.status
      };
    }

    if (!response.ok) {
      console.error('[API Error]', {
        status: response.status,
        data,
        url,
        method: options.method
      });
      
      // Special handling for 403 Forbidden errors which may indicate authorization issues
      if (response.status === 403) {
        console.error('[Auth Error] 403 Forbidden - The server understood the request but refuses to authorize it');
        console.error('This may indicate: (1) Invalid/expired token, (2) Token with insufficient permissions, or (3) CORS issues');
        
        // If we're running in the client, attempt to get a new token by forcing a refresh
        if (typeof window !== 'undefined' && localStorage.getItem('auth_token_refresh')) {
          console.log('Attempting to refresh auth token on 403 error...');
          // For now, we'll just log this - in a real implementation we would refresh the token
          // and retry the request with the new token
        }
      }
      
      // Use the error interceptor to handle common error cases
      handleApiError(response.status, url);
      
      return {
        error: data?.message || data?.error || JSON.stringify(data) || 'An unknown error occurred',
        status: response.status,
      };
    }

    return {
      data,
      status: response.status,
    };
  } catch (error) {
    console.error('API request failed:', error);
    return {
      error: error instanceof Error ? error.message : 'Network error',
      status: 500,
    };
  }
}

/**
 * Server API client with methods for different HTTP verbs
 */
export const serverApiClient = {
  async get<T>(endpoint: string, options?: RequestInit): Promise<ApiResponse<T>> {
    return fetchApi<T>(endpoint, { ...options, method: 'GET' });
  },
  
  async post<T>(endpoint: string, data?: any, options?: RequestInit): Promise<ApiResponse<T>> {
    return fetchApi<T>(endpoint, { 
      ...options, 
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  },
  
  async put<T>(endpoint: string, data?: any, options?: RequestInit): Promise<ApiResponse<T>> {
    // Ensure proper JSON content
    const headers = options?.headers || {};
    const combinedOptions = { 
      ...options, 
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...headers
      }
    };
    
    console.log(`[API Debug] PUT ${endpoint} payload:`, data);
    return fetchApi<T>(endpoint, combinedOptions);
  },
  
  async patch<T>(endpoint: string, data?: any, options?: RequestInit): Promise<ApiResponse<T>> {
    return fetchApi<T>(endpoint, { 
      ...options, 
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
    });
  },
  
  async delete<T>(endpoint: string, options?: RequestInit): Promise<ApiResponse<T>> {
    return fetchApi<T>(endpoint, { ...options, method: 'DELETE' });
  }
};
