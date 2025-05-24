'use client';

import { 
  getTeacherById as serverGetTeacherById,
  getCurrentUserProfile as serverGetCurrentUserProfile,
  UserProfileResponse
} from './api-user';

/**
 * Client-side wrapper for user API
 */

export async function getTeacherById(physicalId: string) {
  return serverGetTeacherById(physicalId);
}

export async function getCurrentUserProfile() {
  return serverGetCurrentUserProfile();
}

// Re-export types
export type { UserProfileResponse };
