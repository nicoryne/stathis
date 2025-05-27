'use client';

import { LoginFormValues, SignUpFormValues, ForgotPasswordFormValues } from '@/lib/validations/auth';
import { setCookie, getCookie, deleteCookie } from '@/lib/utils/cookies';
import { 
  signUp as serverSignUp,
  loginWithEmail as serverLoginWithEmail,
  signOut as serverSignOut,
  forgotPassword as serverForgotPassword,
  resendEmailVerification as serverResendEmailVerification,
  isUserVerified as serverIsUserVerified,
  getUserDetails as serverGetUserDetails,
  loginWithOAuth as serverLoginWithOAuth
} from './api-auth';

// Define provider types for OAuth
export type Provider = 'google' | 'github' | 'microsoft' | 'azure';

/**
 * Client-side wrapper for server authentication actions
 */

export async function signUp(form: SignUpFormValues) {
  return serverSignUp(form);
}

export async function loginWithEmail(form: LoginFormValues) {
  const result = await serverLoginWithEmail(form);
  
  // Store the token in localStorage and cookies for client-side usage
  if (result && typeof result === 'object' && 'accessToken' in result) {
    // Store in localStorage
    localStorage.setItem('auth_token', result.accessToken as string);
    
    // Store in cookies for middleware
    setCookie('auth_token', result.accessToken as string);
    
    // Also store refresh token if needed
    if ('refreshToken' in result) {
      localStorage.setItem('auth_token_refresh', result.refreshToken as string);
      setCookie('auth_token_refresh', result.refreshToken as string);
    }
    
    // Store basic user information in localStorage for getUserDetails
    if (form.email) {
      localStorage.setItem('user_email', form.email);
    }
    
    // Additional user information if available in the result
    if ('user' in result && typeof result.user === 'object') {
      const user = result.user as any;
      if (user.firstName && user.lastName) {
        localStorage.setItem('user_name', `${user.firstName} ${user.lastName}`);
      }
      if (user.role) {
        localStorage.setItem('user_role', user.role);
      }
      if (user.physicalId) {
        localStorage.setItem('user_physical_id', user.physicalId);
      }
    }
    
    // If we don't have the user info in the response, we need to parse it from the JWT token
    if (!('user' in result) || !result.user) {
      try {
        const token = result.accessToken as string;
        const parts = token.split('.');
        if (parts.length === 3) {
          const payload = parts[1];
          const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
          const jsonPayload = decodeURIComponent(
            atob(base64)
              .split('')
              .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
              .join('')
          );
          
          const decodedToken = JSON.parse(jsonPayload);
          if (decodedToken.physicalId) {
            localStorage.setItem('user_physical_id', decodedToken.physicalId);
          }
        }
      } catch (error) {
        console.error('Error parsing JWT token:', error);
      }
    }
  }
  
  return result;
}

export async function loginWithOAuth(provider: Provider) {
  return serverLoginWithOAuth(provider);
}

export async function signOut() {
  try {
    // First get the token before clearing it
    const token = localStorage.getItem('auth_token');
    
    // Clear token from localStorage and cookies
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_token_refresh');
    deleteCookie('auth_token');
    deleteCookie('auth_token_refresh');
    
    // Also clear user information
    localStorage.removeItem('user_email');
    localStorage.removeItem('user_name');
    localStorage.removeItem('user_role');
    
    // Call the server logout function with the token
    if (token) {
      await serverSignOut();
    }
    
    // Force a page refresh to ensure all state is cleared and redirects work
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
    
    return { success: true };
  } catch (error) {
    console.error('Error during signOut:', error);
    
    // Even if there's an error, redirect to login
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
    
    return { success: false, error };
  }
}

export async function forgotPassword(form: ForgotPasswordFormValues) {
  return serverForgotPassword(form);
}

export async function resendEmailVerification(email: string) {
  return serverResendEmailVerification(email);
}

export async function getUserDetails() {
  return serverGetUserDetails();
}

export async function isUserVerified() {
  return serverIsUserVerified();
}

// Helper function to check if user is authenticated on client-side
export function isAuthenticated() {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('auth_token') || getCookie('auth_token');
    console.log('Auth token found:', !!token);
    return !!token;
  }
  return false;
}

// Helper function to get the token on client-side
export function getToken() {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('auth_token');
  }
  return null;
}

// Helper function to get the refresh token on client-side
export function getRefreshToken() {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('auth_token_refresh');
  }
  return null;
}
