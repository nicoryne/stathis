import { v4 as uuidv4 } from "uuid";
import { ClassroomFormValues } from "@/lib/validations/classroom";
import { createBrowserClient } from '@supabase/ssr';

export const createClassroom = async (form: ClassroomFormValues) => {
  const supabase = createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL || '',
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
  );
  
  // Get user details from client session
  const { data: { user } } = await supabase.auth.getUser();
  
  const name = form.name;
  const description = form.description;

  const { error } = await supabase.from('classrooms').insert({
    id: uuidv4(),
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
    name: name,
    description: description,
    is_active: true,
    teacher_id: user?.id,
  });
  
  if (error) {
    throw new Error(error.message);
  }
}; 