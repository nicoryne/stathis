// Remove 'use server' directive since we're using browser client
// 'use server';

import { createBrowserClient } from '@supabase/ssr';

// Get all vitals for a specific class
export const getClassVitals = async (classId: string) => {
  if (!classId) {
    return { data: null, error: new Error('Class ID is required') };
  }

  try {
    const supabase = createBrowserClient(
      process.env.NEXT_PUBLIC_SUPABASE_URL || '',
      process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
    );

    const { data, error } = await supabase
      .from('vitals')
      .select('*')
      .eq('class_id', classId);

    if (error) {
      console.error('Error fetching vitals:', error);
      return { data: null, error };
    }

    return { data, error: null };
  } catch (err) {
    console.error('Unexpected error in getClassVitals:', err);
    return { data: null, error: err instanceof Error ? err : new Error('Unknown error') };
  }
};