/**
 * Server-side API client for backend requests
 */
export const API_BASE_URL = 'http://localhost:8080/api';

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
    
    // Add authentication token if available (client-side only)
    if (typeof window !== 'undefined') {
      // Get auth token
      const authToken = localStorage.getItem('auth_token');
      
      // Get refresh token
      const refreshToken = localStorage.getItem('auth_token_refresh');
                        
      if (authToken) {
        // Set authorization header
        headers['Authorization'] = `Bearer ${authToken}`;
        
        // Log detailed info for debugging
        console.log('Added auth token to request', { 
          tokenType: 'Bearer', 
          tokenLength: authToken.length,
          tokenStart: authToken.substring(0, 10) + '...',
          tokenEnd: '...' + authToken.substring(authToken.length - 10),
          headers: headers
        });
        
        try {
          // Try to decode the token to check its contents
          const tokenParts = authToken.split('.');
          if (tokenParts.length === 3) {
            const payload = JSON.parse(atob(tokenParts[1].replace(/-/g, '+').replace(/_/g, '/')));
            console.log('JWT token payload:', payload);
          }
        } catch (e) {
          console.error('Error decoding token:', e);
        }
      } else {
        console.warn('No auth token found in localStorage');
      }
      
      // Include refresh token in headers if available
      if (refreshToken) {
        headers['X-Refresh-Token'] = refreshToken;
        console.log('Added refresh token to request');
      }
      
      // Add to URL params for endpoints that expect it as a parameter
      // ONLY do this for auth-specific endpoints
      if (endpoint.includes('/auth/')) {
        if (endpoint.includes('?')) {
          endpoint += `&refreshToken=${refreshToken || ''}`;
        } else if (endpoint.includes('/refresh') || endpoint.includes('/token')) {
          endpoint += `?refreshToken=${refreshToken || ''}`;
        }
      }
    }

    // Always include credentials to ensure cookies are sent
    const response = await fetch(url, {
      ...options,
      headers,
      credentials: 'include', // This ensures cookies are sent with the request
      mode: 'cors'           // Explicitly use CORS mode
    });

    console.log(`[API Response] Status: ${response.status}`);

    // Try to parse JSON response
    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    console.log('[API Response Data]', data);

    if (!response.ok) {
      console.error('[API Error]', {
        status: response.status,
        data,
        url,
        method: options.method
      });
      return {
        error: data.message || data.error || JSON.stringify(data) || 'An unknown error occurred',
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
    return fetchApi<T>(endpoint, { 
      ...options, 
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
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
