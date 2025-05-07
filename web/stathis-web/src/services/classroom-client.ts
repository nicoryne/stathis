import { v4 as uuidv4 } from "uuid";
import { ClassroomFormValues } from "@/lib/validations/classroom";
import { createBrowserClient } from '@supabase/ssr';

export const createClassroom = async (form: ClassroomFormValues) => {
  const supabase = createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL || '',
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
  );
  
  // Get user details from client session
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  
  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }
  
  console.log('Authenticated user:', user.id);
  
  const name = form.name;
  const description = form.description;

  try {
    const { error } = await supabase.from('classrooms').insert({
      id: uuidv4(),
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
      name: name,
      description: description,
      is_active: true,
      teacher_id: user.id,
    });
    
    if (error) {
      console.error('Supabase error:', error);
      throw new Error(error.message);
    }
  } catch (error) {
    console.error('Insert error:', error);
    throw error;
  }
}; 