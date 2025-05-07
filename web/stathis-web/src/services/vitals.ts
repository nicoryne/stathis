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

// Get all vitals for a specific student
export const getStudentVitals = async (studentId: string) => {
  const supabase = createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL || '',
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
  );

  const { data, error } = await supabase
    .from('vitals')
    .select('*')
    .eq('student_id', studentId)

  return { data, error };
};