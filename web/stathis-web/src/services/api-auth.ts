'use server';

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
  const { data, error, status } = await serverApiClient.post('/auth/register', {
    email: form.email,
    password: form.password,
    firstName: form.firstName,
    lastName: form.lastName,
    userRole: 'TEACHER'  // Using the correct field name and enum value expected by backend
  });
  
  if (error) {
    console.error('[Auth Signup Error]', { error, status, data });
    throw new Error(error);
  }

  return data;
};

/**
 * Login with email and password
 */
export async function loginWithEmail(form: LoginFormValues) {
  const { data, error, status } = await serverApiClient.post('/auth/login', {
    email: form.email,
    password: form.password
  });

  if (error) {
    console.error('[Auth Login Error]', { error, status, data });
    throw new Error(error);
  }

  // Store token in localStorage on client side
  if (typeof window !== 'undefined' && data && typeof data === 'object' && 'accessToken' in data) {
    localStorage.setItem('auth_token', data.accessToken as string);
    // Also store refresh token if needed
    if ('refreshToken' in data) {
      localStorage.setItem('auth_token_refresh', data.refreshToken as string);
    }
  }

  return data;
};

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
