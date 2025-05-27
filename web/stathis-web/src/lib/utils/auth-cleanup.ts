/**
 * Auth token cleanup utility
 * Standardizes all tokens into a single format
 */

// The key we want to standardize on
const STANDARD_TOKEN_KEY = 'auth_token';

/**
 * Consolidates all authentication tokens into a single standardized format
 * and removes duplicate tokens
 */
export function consolidateAuthTokens() {
  if (typeof window === 'undefined') return;
  
  // All possible token keys used in the application
  const possibleTokenKeys = [
    'auth_token',
    'auth_Token',
    'authToken',
    'token'
  ];
  
  // Find the first valid token
  let primaryToken: string | null = null;
  
  // Check auth_Token first as it has the TEACHER role which is needed for most API calls
  primaryToken = localStorage.getItem('auth_Token');
  
  // If no primary token found, try others
  if (!primaryToken) {
    for (const key of possibleTokenKeys) {
      const token = localStorage.getItem(key);
      if (token && key !== 'auth_Token') {
        primaryToken = token;
        break;
      }
    }
  }
  
  // If we found a token, standardize on our chosen key
  if (primaryToken) {
    // Set the standard token
    localStorage.setItem(STANDARD_TOKEN_KEY, primaryToken);
    
    // Remove all other tokens to avoid confusion
    for (const key of possibleTokenKeys) {
      if (key !== STANDARD_TOKEN_KEY) {
        localStorage.removeItem(key);
      }
    }
    
    console.log('Auth tokens consolidated to standard format:', STANDARD_TOKEN_KEY);
    return primaryToken;
  }
  
  console.warn('No auth token found to consolidate');
  return null;
}

/**
 * Runs the token consolidation and returns the standardized token
 */
export function getStandardAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  
  // Get the token from our standard location
  let token = localStorage.getItem(STANDARD_TOKEN_KEY);
  
  // If not found, run the consolidation process
  if (!token) {
    token = consolidateAuthTokens() || null;
  }
  
  return token;
}
