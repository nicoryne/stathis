'use server';

import { createBrowserClient } from '@supabase/ssr';

// Get all vitals for a specific class
export const getClassVitals = async (classId: string) => {
  const supabase = createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL || '',
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
  );



  const { data, error } = await supabase
    .from('vitals')
    .select('*')
    .eq('class_id', classId)

  return { data, error };
};