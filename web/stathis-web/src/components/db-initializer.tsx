'use client';

import { useEffect, useState } from 'react';
import { initializeDatabase } from '@/lib/supabase/db-init';

export function DbInitializer() {
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    const initialize = async () => {
      try {
        const result = await initializeDatabase();
        setInitialized(result.success);
        
        if (!result.success) {
          console.error('Database initialization failed', result.error);
        }
      } catch (error) {
        console.error('Error initializing database:', error);
      }
    };

    initialize();
  }, []);

  // This component doesn't render anything
  return null;
} 