'use client';

import { useEffect } from 'react';
import { consolidateAuthTokens } from '@/lib/utils/auth-cleanup';

/**
 * Component that runs initialization code for authentication
 * This includes cleaning up duplicate tokens
 */
export function AuthInitializer() {
  useEffect(() => {
    // Run on client-side only
    if (typeof window !== 'undefined') {
      // Clean up duplicate auth tokens
      consolidateAuthTokens();
    }
  }, []);

  // This component doesn't render anything
  return null;
}
