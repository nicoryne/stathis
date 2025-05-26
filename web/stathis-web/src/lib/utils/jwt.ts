/**
 * JWT token utilities
 */

/**
 * Decode a JWT token without verification
 * This is safe for client-side as we're only extracting the payload
 * @param token JWT token string
 * @returns Decoded payload or null if invalid
 */
export function decodeJwt<T = any>(token: string): T | null {
  try {
    // JWT has three parts: header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }
    
    // Base64Url decode the payload (middle part)
    const payload = parts[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Error decoding JWT:', error);
    return null;
  }
}

/**
 * Get user information from JWT token
 * @returns User info from token or null if not available
 */
export function getUserFromToken() {
  if (typeof window === 'undefined') {
    return null;
  }
  
  const token = localStorage.getItem('auth_token');
  if (!token) {
    return null;
  }
  
  return decodeJwt<{
    sub: string; // email
    role: string;
    exp: number;
    iat: number;
  }>(token);
}

/**
 * Get current user's email from JWT token
 * @returns User's email or null if not available
 */
export function getCurrentUserEmail(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  
  // First try to get from localStorage
  const storedEmail = localStorage.getItem('user_email');
  if (storedEmail) {
    return storedEmail;
  }
  
  // If not in localStorage, try to get from JWT payload
  const userInfo = getUserFromToken();
  return userInfo?.sub || null; // 'sub' field contains the email
}

/**
 * Get current user's role from JWT token
 * @returns User's role or null if not available
 */
export function getCurrentUserRole(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  
  // First try to get from localStorage
  const storedRole = localStorage.getItem('user_role');
  if (storedRole) {
    return storedRole;
  }
  
  // If not in localStorage, try to get from JWT payload
  const userInfo = getUserFromToken();
  return userInfo?.role || null;
}

/**
 * Generate a deterministic physical ID based on an email
 * This creates a ID in the format XX-XXXX-XXX where X is a digit
 * The ID will be consistent for the same email
 * 
 * @param email User's email address
 * @returns A physical ID in the format XX-XXXX-XXX
 */
export function generatePhysicalIdFromEmail(email: string): string {
  if (!email) return '00-0000-000';
  
  console.log('Generating physical ID for email:', email);
  
  // Create a simple hash of the email
  const emailHash = Array.from(email)
    .reduce((hash, char) => ((hash << 5) - hash) + char.charCodeAt(0), 0);
  const positiveHash = Math.abs(emailHash);
  
  // Format as XX-XXXX-XXX to match the backend's expected pattern
  const part1 = String(positiveHash % 100).padStart(2, '0');
  const part2 = String(positiveHash % 10000).padStart(4, '0');
  const part3 = String(positiveHash % 1000).padStart(3, '0');
  
  // Use correct format based on backend expectations
  // The validation pattern in ClassroomBodyDTO expects ROOM-XX-XXXX-XXX
  const physicalId = `ROOM-${part1}-${part2}-${part3}`;
  console.log('Generated physical ID:', physicalId);
  
  return physicalId;
}

/**
 * Get current user's physical ID using a deterministic algorithm
 * This generates a consistent ID for each email address
 * 
 * @returns Generated physical ID for the current user or null if not logged in
 */
export function getCurrentUserPhysicalId(): string | null {
  const email = getCurrentUserEmail();
  if (!email) return null;
  
  return generatePhysicalIdFromEmail(email);
}
