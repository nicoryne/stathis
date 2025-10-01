'use client';

/**
 * Error interceptor for API responses
 * Handles common error cases like authentication failures
 */

/**
 * Process API error responses and take appropriate actions
 * @param status HTTP status code
 * @param url Request URL
 * @returns True if the error was handled, false otherwise
 */
export function handleApiError(status: number, url: string): boolean {
  // Handle authentication errors
  if (status === 401) {
    console.error('[Auth Error] Unauthorized access (401)', { url });
    
    // Check if token is present
    const token = localStorage.getItem('auth_token');
    if (!token) {
      console.error('[Auth Error] No authentication token found');
      redirectToLogin();
      return true;
    }
    
    // Token might be expired - attempt refresh if a refresh endpoint exists
    // For now just log it
    console.error('[Auth Error] Token might be expired or invalid');
    return false;
  }
  
  // Handle authorization errors
  if (status === 403) {
    console.error('[Auth Error] Forbidden access (403)', { url });
    
    // Debug token information
    const token = localStorage.getItem('auth_token');
    if (token) {
      try {
        const tokenParts = token.split('.');
        if (tokenParts.length === 3) {
          const payload = JSON.parse(atob(tokenParts[1].replace(/-/g, '+').replace(/_/g, '/')));
          console.error('[Auth Debug] Token payload:', payload);
          
          // Check if token is expired
          const expiration = payload.exp * 1000; // Convert to milliseconds
          const now = Date.now();
          if (expiration < now) {
            console.error('[Auth Error] Token is expired', {
              expiration: new Date(expiration).toISOString(),
              now: new Date(now).toISOString(),
              difference: Math.floor((now - expiration) / 1000 / 60) + ' minutes'
            });
          }
        }
      } catch (e) {
        console.error('[Auth Debug] Failed to parse token:', e);
      }
    } else {
      console.error('[Auth Error] No token found but endpoint requires authorization');
    }
    
    return false;
  }
  
  return false;
}

/**
 * Redirect to login page
 */
function redirectToLogin() {
  if (typeof window !== 'undefined') {
    // Store the current page so we can redirect back after login
    const currentPath = window.location.pathname;
    localStorage.setItem('login_redirect', currentPath);
    
    // Redirect to login
    window.location.href = '/login';
  }
}
