'use server';

import { createClient } from './server';

export async function initializeDatabase() {
  const supabase = await createClient();
  console.log('Initializing database schema...');

  try {
    // Check if task_template table exists
    const { error: checkError } = await (supabase as any)
      .from('task_template')
      .select('id')
      .limit(1);

    // If there's an error saying the table doesn't exist, create it
    if (checkError && checkError.message.includes('does not exist')) {
      console.log('Creating task_template table...');
      
      // Create the task_template table
      const { error: createError } = await (supabase as any).rpc('create_task_template_table', {});
      
      if (createError) {
        console.error('Error creating task_template table:', createError);
        
        // If RPC fails, try direct SQL (requires higher privileges)
        const { error: sqlError } = await (supabase as any).rpc('execute_sql', {
          sql_query: `
            CREATE TABLE IF NOT EXISTS public.task_template (
              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
              created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
              updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
              name TEXT NOT NULL,
              description TEXT,
              content JSONB,
              creator_id UUID REFERENCES auth.users(id)
            );
            
            -- Add RLS policies
            ALTER TABLE public.task_template ENABLE ROW LEVEL SECURITY;
            
            -- Allow teachers to view any template
            CREATE POLICY "Teachers can view any template" ON public.task_template
              FOR SELECT USING (
                EXISTS (
                  SELECT 1 FROM public.profiles
                  WHERE profiles.id = auth.uid()
                  AND profiles.account_type = 'teacher'
                )
              );
              
            -- Allow teachers to create templates
            CREATE POLICY "Teachers can create templates" ON public.task_template
              FOR INSERT WITH CHECK (
                EXISTS (
                  SELECT 1 FROM public.profiles
                  WHERE profiles.id = auth.uid()
                  AND profiles.account_type = 'teacher'
                )
              );
              
            -- Allow teachers to update their own templates
            CREATE POLICY "Teachers can update their own templates" ON public.task_template
              FOR UPDATE USING (
                creator_id = auth.uid() AND
                EXISTS (
                  SELECT 1 FROM public.profiles
                  WHERE profiles.id = auth.uid()
                  AND profiles.account_type = 'teacher'
                )
              );
              
            -- Allow teachers to delete their own templates
            CREATE POLICY "Teachers can delete their own templates" ON public.task_template
              FOR DELETE USING (
                creator_id = auth.uid() AND
                EXISTS (
                  SELECT 1 FROM public.profiles
                  WHERE profiles.id = auth.uid()
                  AND profiles.account_type = 'teacher'
                )
              );
          `
        });
        
        if (sqlError) {
          console.error('Error executing SQL:', sqlError);
        } else {
          console.log('Successfully created task_template table via SQL');
        }
      } else {
        console.log('Successfully created task_template table via RPC');
      }
    } else {
      console.log('task_template table already exists');
    }

    return { success: true };
  } catch (error) {
    console.error('Error initializing database:', error);
    return { success: false, error };
  }
} 