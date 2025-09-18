// Client-side API implementation

import { serverApiClient } from '@/lib/api/server-client';
import {
  ForgotPasswordFormValues,
  LoginFormValues,
  SignUpFormValues
} from '@/lib/validations/auth';
// These are server components imports in Next.js
const revalidatePath = async (path: string) => {
  // Placeholder for server action
  console.log(`Revalidating path: ${path}`);
};

const redirect = (path: string) => {
  // Will be handled client-side
  if (typeof window !== 'undefined') {
    window.location.href = path;
  }
};

// Define provider types for OAuth
export type Provider = 'google' | 'github' | 'microsoft' | 'azure';

/**
 * Register a new user
 */
export async function signUp(form: SignUpFormValues) {
  try {
    console.log('[Auth] Attempting to register user:', form.email);
    
    // Try a simplified payload structure
    // The userRole might need to be sent differently for the Java enum
    const payload = {
      email: form.email,
      password: form.password,
      firstName: form.firstName,
      lastName: form.lastName,
      userRole: 'TEACHER'
    };
    
    console.log('[Auth] Registration payload:', JSON.stringify(payload));
    
    // Try making the request with native fetch instead of our client
    // This bypasses any potential issues with our custom client
    try {
      console.log('[Auth] Trying direct fetch approach');
      const response = await fetch('https://api-stathis.ryne.dev/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload),
        credentials: 'include',
        mode: 'cors'
      });
      
      console.log('[Auth] Registration response status:', response.status);
      
      // Clone the response before reading the body to avoid the 'body stream already read' error
      const responseClone = response.clone();
      
      let data;
      try {
        data = await response.json();
      } catch (parseError) {
        // If JSON parsing fails, try reading as text from the cloned response
        console.log('[Auth] JSON parsing failed, trying text instead');
        data = await responseClone.text();
      }
      
      console.log('[Auth] Registration response data:', data);
      
      // Extra logging to diagnose response handling
      console.log('[Auth] Response OK:', response.ok);
      console.log('[Auth] Response type:', typeof data);
      
      if (!response.ok) {
        const status = response.status;
        let errorMessage = 'Registration failed';
        let isEmailInUseError = false;
        
        // Better error extraction logic
        if (typeof data === 'string') {
          // Handle case where data might be a string
          errorMessage = data || errorMessage;
          
          // Check if this is an 'email already in use' error
          isEmailInUseError = data.includes('Email is already in use') || 
                             data.includes('already exists') || 
                             data.includes('already registered');
        } else if (data) {
          // Extract error message from data object with fallbacks
          // Use type assertion to avoid TypeScript errors
          const responseData = typeof data === 'object' ? (data as Record<string, any>) : {};
          errorMessage = responseData.message || responseData.error || responseData.errorMessage || 
                        (responseData.errors && JSON.stringify(responseData.errors)) || errorMessage;
          
          // Check if response or error message suggests email is already in use
          // First check the parsed message
          if (errorMessage) {
            isEmailInUseError = errorMessage.includes('Email is already in use') || 
                               errorMessage.includes('already exists') || 
                               errorMessage.includes('already registered');
          }
          
          // Also check if the backend returned a specific status code for this case
          if (status === 409 || (status === 400 && responseData.code === 'EMAIL_ALREADY_EXISTS')) {
            isEmailInUseError = true;
            errorMessage = 'Email is already in use. Please verify your account or try logging in.';
          }
        }
        
        // Special case for 401 response with empty body which might indicate duplicate email
        if (status === 401 && (!data || (typeof data === 'string' && data.trim() === ''))) {
          // This might be a duplicate email case that's not properly communicating the error
          console.log('[Auth] Empty 401 response - might be duplicate email');
          isEmailInUseError = true;
          errorMessage = 'Email may already be registered. Please verify your account or try logging in.';
        }
        
        // Add detailed logging
        console.error('[Auth Signup Error]', { 
          status, 
          errorMessage, 
          isEmailInUseError,
          responseData: data,
          url: response.url,
          statusText: response.statusText
        });
        
        // For email already in use errors, throw a special error
        if (isEmailInUseError) {
          const error = new Error('Email is already in use');
          error.name = 'EmailAlreadyInUseError';
          throw error;
        }
        
        throw new Error(errorMessage);
      }
      
      console.log('[Auth] Registration successful for:', form.email);
      return data;
    } catch (fetchError) {
      console.error('[Auth] Direct fetch failed:', fetchError);
      
      // Fall back to our client as a second attempt
      console.log('[Auth] Trying fallback with server API client');
      const { data, error, status } = await serverApiClient.post('/auth/register', payload, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      console.log('[Auth] Fallback response:', { data, status });
      
      if (error) {
        let errorMessage = error;
        let isEmailInUseError = false;
        
        // Add better error extraction for the fallback method too
        if (typeof data === 'object' && data !== null) {
          // Use type assertion to avoid TypeScript errors
          const responseData = data as Record<string, any>;
          errorMessage = responseData.message || responseData.error || responseData.errorMessage || 
                       (responseData.errors && JSON.stringify(responseData.errors)) || error;
        }
        
        // Check if this is a duplicate email error
        if (typeof errorMessage === 'string') {
          isEmailInUseError = errorMessage.includes('Email is already in use') || 
                            errorMessage.includes('already exists') || 
                            errorMessage.includes('already registered');
        }
        
        // Special case for 401 response with empty message which might indicate duplicate email
        if (status === 401 && (!error || error === '')) {
          isEmailInUseError = true;
          errorMessage = 'Email may already be registered. Please verify your account or try logging in.';
        }
        
        console.error('[Auth Signup Error (Fallback)]', { 
          errorMessage, 
          isEmailInUseError,
          status, 
          responseData: data 
        });
        
        // For email already in use errors, throw a special error
        if (isEmailInUseError) {
          const customError = new Error('Email is already in use');
          customError.name = 'EmailAlreadyInUseError';
          throw customError;
        }
        
        throw new Error(errorMessage);
      }
      
      console.log('[Auth] Registration successful for:', form.email);
      return data;
    }
  } catch (e) {
    console.error('[Auth] Registration failed with exception:', e);
    throw new Error(e instanceof Error ? e.message : 'Registration failed - check browser console for details');
  }
}

/**
 * Login with email and password
 */
export async function loginWithEmail(form: LoginFormValues) {
  try {
    console.log('[Auth] Attempting to login user:', form.email);
    
    const { data, error, status } = await serverApiClient.post('/auth/login', {
      email: form.email,
      password: form.password
    });

    if (error) {
      console.error('[Auth Login Error]', { error, status, data });
      
      // Handle specific HTTP status codes with user-friendly messages
      if (status === 401 || status === 403) {
        throw new Error('Invalid email or password. Please check your credentials and try again.');
      }
      
      // Check for common error message patterns
      if (typeof error === 'string') {
        if (error.toLowerCase().includes('invalid') || 
            error.toLowerCase().includes('incorrect') || 
            error.toLowerCase().includes('not found')) {
          throw new Error('Invalid email or password. Please check your credentials and try again.');
        }
      }
      
      throw new Error(error);
    }

    console.log('[Auth] Login successful for:', form.email);
    // Store token in localStorage on client side
    if (typeof window !== 'undefined' && data && typeof data === 'object' && 'accessToken' in data) {
      localStorage.setItem('auth_token', data.accessToken as string);
      // Also store refresh token if needed
      if ('refreshToken' in data) {
        localStorage.setItem('auth_token_refresh', data.refreshToken as string);
      }
    }

    return data;
  } catch (e) {
    console.error('[Auth] Login failed with exception:', e);
    if (e instanceof Error) {
      // If we already have a formatted error message, use it
      throw e;
    } else {
      // Generic fallback error
      throw new Error('Login failed. Please check your credentials and try again.');
    }
  }
}

/**
 * Login with OAuth provider
 */
export async function loginWithOAuth(provider: Provider): Promise<void> {
  const { data, error, status } = await serverApiClient.post('/auth/oauth', { provider });

  if (error) {
    throw new Error(error);
  }

  // If the backend returns an authorization URL, redirect to it
  if (data && typeof data === 'object' && 'authorizationUrl' in data) {
    window.location.href = data.authorizationUrl as string;
  }
};

/**
 * Sign out the current user
 */
export async function signOut() {
  // Get the current token before we clear it
  let token = null;
  if (typeof window !== 'undefined') {
    token = localStorage.getItem('auth_token');
  }
  
  try {
    // Only call the backend logout endpoint if we have a token
    if (token) {
      const { error } = await serverApiClient.post('/auth/logout', {}, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (error) {
        console.error('Error during logout API call:', error);
      }
    } else {
      console.log('No authentication token found, skipping backend logout call');
    }
  } catch (err) {
    // Catch and log any network or server errors
    console.error('Failed to call logout endpoint:', err);
  } finally {
    // Always clean up the tokens and user info on the client side regardless of backend errors
    if (typeof window !== 'undefined') {
      // Remove all token keys
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_token_refresh');
      
      // Also clear user information
      localStorage.removeItem('user_email');
      localStorage.removeItem('user_name');
      localStorage.removeItem('user_role');
    }
  }

  // Force path revalidation and redirect
  revalidatePath('/');
  redirect('/');
};

/**
 * Request password reset email
 */
export async function forgotPassword(form: ForgotPasswordFormValues) {
  const { data, error, status } = await serverApiClient.post('/auth/forgot-password', {
    email: form.email
  });

  if (error) {
    return { error };
  }

  return { data };
};

/**
 * Resend email verification
 */
export async function resendEmailVerification(email: string) {
  const { data, error, status } = await serverApiClient.post('/auth/resend-verification', {
    email
  });

  if (error) {
    return { error };
  }

  return { data };
};

/**
 * Get current user details
 * Note: Since there's no direct /auth/me endpoint, we're constructing user details
 * from the stored information or token claims. This is a temporary solution until
 * a proper endpoint is available.
 */
export async function getUserDetails() {
  try {
    // Since we don't have a /auth/me endpoint, we'll check if we're authenticated
    // and return basic user information from the token or localStorage
    
    // Check if we have an auth token
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('auth_token');
      
      if (!token) {
        console.log('No auth token found, user is not authenticated');
        return null;
      }
      
      // Try to get any user information from localStorage that might have been saved during login
      const userEmail = localStorage.getItem('user_email');
      const userRole = localStorage.getItem('user_role');
      const userName = localStorage.getItem('user_name');
      
      // Return available user info
      return {
        authenticated: true,
        email: userEmail,
        role: userRole,
        name: userName,
        // Add other fields as needed
      };
    }
    
    return null;
  } catch (error) {
    console.error('getUserDetails error:', error);
    // Return null instead of throwing to prevent app crashes
    return null;
  }
};

/**
 * Check if user's email is verified
 */
export async function isUserVerified() {
  try {
    const { data, error, status } = await serverApiClient.get('/auth/verify-status');

    if (error) {
      console.error('Error checking verification status:', { error, status });
      return false; // Assume not verified on error
    }

    return data && typeof data === 'object' && 'verified' in data ? data.verified === true : false;
  } catch (error) {
    console.error('isUserVerified error:', error);
    return false; // Assume not verified on error
  }
};
