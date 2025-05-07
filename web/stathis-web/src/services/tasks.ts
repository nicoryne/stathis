'use server';

import { v4 as uuidv4 } from "uuid";
import { createClient } from '@/lib/supabase/server';
import { TaskFormValues } from "@/lib/validations/tasks";

// Get tasks for a specific classroom or for all classrooms the teacher has
export const getTeacherTasks = async (classroomId?: string) => {
  const supabase = await createClient();
  
  // Get the current user
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }
  
  console.log('Current user ID:', user.id);
  
  try {
    // First get all classrooms where the user is a teacher
    let classroomsQuery = supabase
      .from('classrooms')
      .select('id')
      .eq('teacher_id', user.id);
    
    // If filtering by classroom, only get that specific classroom
    // (still checking that the user is the teacher)
    if (classroomId) {
      classroomsQuery = classroomsQuery.eq('id', classroomId);
    }
    
    const { data: classrooms, error: classroomsError } = await classroomsQuery;
    
    if (classroomsError) {
      console.error('Error fetching classrooms:', classroomsError);
      throw new Error('Failed to fetch classrooms');
    }
    
    console.log('Found classrooms:', classrooms);
    
    if (!classrooms || classrooms.length === 0) {
      console.log('No classrooms found for this teacher');
      return [];
    }
    
    // Get classroom IDs
    const classroomIds = classrooms.map(classroom => classroom.id);
    console.log('Classroom IDs to search for tasks:', classroomIds);
    
    // Direct query to check if any tasks exist at all
    const { data: allTasks, error: allTasksError } = await (supabase as any)
      .from('tasks')
      .select('id, name, classroom_id')
      .limit(10);
    
    console.log('Direct check for any tasks in the database:', allTasks);
    if (allTasksError) {
      console.error('Error in direct tasks check:', allTasksError);
    }
    
    // Query tasks with current user's classroom filter
    const { data: tasks, error: tasksError } = await (supabase as any)
      .from('tasks')
      .select('*, classroom:classroom_id(*)')
      .in('classroom_id', classroomIds)
      .order('created_at', { ascending: false });
    
    if (tasksError) {
      console.error('Error fetching tasks:', tasksError);
      throw new Error('Failed to fetch tasks');
    }
    
    console.log('Found tasks for teacher classrooms:', tasks ? tasks.length : 0);
    
    return tasks || [];
  } catch (error) {
    console.error('Error in getTeacherTasks:', error);
    throw error;
  }
};

// Get a specific task by ID
export const getTaskById = async (taskId: string) => {
  const supabase = await createClient();
  
  // Get the current user
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }
  
  try {
    // Get the task
    const { data: task, error: taskError } = await (supabase as any)
      .from('tasks')
      .select('*, classroom:classroom_id(*)')
      .eq('id', taskId)
      .single();
    
    if (taskError) {
      console.error('Error fetching task:', taskError);
      throw new Error('Failed to fetch task');
    }
    
    // Verify the user is the teacher of the classroom
    const classroom = task.classroom;
    if (classroom?.teacher_id !== user.id) {
      throw new Error('Unauthorized: You are not the teacher of this classroom');
    }
    
    return task;
  } catch (error) {
    console.error('Error in getTaskById:', error);
    throw error;
  }
};

// Create a new task
export const createTask = async (form: TaskFormValues) => {
  const supabase = await createClient();
  
  // Get the current user
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }
  console.log('Creating task for user:', user.id);
  
  try {
    // We're skipping the table existence check here since we already check in createTasksTable
    
    // Verify the user is the teacher of the classroom
    const { data: classroom, error: classroomError } = await supabase
      .from('classrooms')
      .select('teacher_id')
      .eq('id', form.classroom_id)
      .single();
    
    if (classroomError) {
      console.error('Error fetching classroom:', classroomError);
      throw new Error('Failed to fetch classroom');
    }
    
    const teacherId = (classroom as any).teacher_id;
    console.log('Classroom teacher ID:', teacherId, 'User ID:', user.id);
    if (teacherId !== user.id) {
      throw new Error('Unauthorized: You are not the teacher of this classroom');
    }
    
    const now = new Date().toISOString();
    const taskId = uuidv4();
    
    // Format dates
    const submission_date = form.submission_date ? new Date(form.submission_date).toISOString() : null;
    const closing_date = form.closing_date ? new Date(form.closing_date).toISOString() : null;
    
    // Handle "none" value as null for task_template_id
    const task_template_id = form.task_template_id && form.task_template_id !== 'none' 
      ? form.task_template_id 
      : null;
    
    // If a task template is selected, fetch its content
    let templateContent = null;
    if (task_template_id) {
      // Determine which table to use (singular or plural)
      const tableName = 'task_template'; // Based on your schema
      
      const { data: template, error: templateError } = await (supabase as any)
        .from(tableName)
        .select('*')
        .eq('id', task_template_id)
        .single();
      
      if (templateError) {
        console.error('Error fetching task template:', templateError);
        throw new Error('Failed to fetch task template');
      }
      
      if (template && template.content) {
        templateContent = template.content;
      }
    }
    
    console.log('Inserting task with data:', {
      id: taskId,
      name: form.name,
      type: form.type,
      classroom_id: form.classroom_id
    });
    
    const { error, data } = await (supabase as any).from('tasks').insert({
      id: taskId,
      created_at: now,
      updated_at: now,
      name: form.name,
      description: form.description || null,
      type: form.type,
      submission_date,
      closing_date,
      image_url: form.image_url || null,
      classroom_id: form.classroom_id,
      task_template_id: task_template_id,
      content: templateContent // Add the content from the template
    }).select();
    
    if (error) {
      console.error('Error creating task:', error);
      throw new Error(error.message);
    }
    
    console.log('Task created successfully:', data);
    return { id: taskId };
  } catch (error) {
    console.error('Error in createTask:', error);
    throw error;
  }
};

// Update an existing task
export const updateTask = async (taskId: string, form: TaskFormValues) => {
  const supabase = await createClient();
  
  // Get the current user
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }
  
  try {
    // Get the task with classroom info
    const { data: task, error: taskError } = await (supabase as any)
      .from('tasks')
      .select('*, classroom:classroom_id(*)')
      .eq('id', taskId)
      .single();
    
    if (taskError) {
      console.error('Error fetching task:', taskError);
      throw new Error('Failed to fetch task');
    }
    
    // Verify the user is the teacher of the classroom
    const classroom = task.classroom;
    if (classroom?.teacher_id !== user.id) {
      throw new Error('Unauthorized: You are not the teacher of this classroom');
    }
    
    // Format dates
    const submission_date = form.submission_date ? new Date(form.submission_date).toISOString() : null;
    const closing_date = form.closing_date ? new Date(form.closing_date).toISOString() : null;
    
    // Handle "none" value as null for task_template_id
    const task_template_id = form.task_template_id && form.task_template_id !== 'none' 
      ? form.task_template_id 
      : null;
    
    // Check if task template has changed
    let content = task.content;
    if (task_template_id && task_template_id !== task.task_template_id) {
      // If template changed, fetch new template content
      const tableName = 'task_template'; // Based on your schema
      
      const { data: template, error: templateError } = await (supabase as any)
        .from(tableName)
        .select('*')
        .eq('id', task_template_id)
        .single();
      
      if (templateError) {
        console.error('Error fetching task template:', templateError);
        throw new Error('Failed to fetch task template');
      }
      
      if (template && template.content) {
        content = template.content;
      }
    } else if (!task_template_id) {
      // If template is removed, set content to null
      content = null;
    }
    
    const { error } = await (supabase as any)
      .from('tasks')
      .update({
        updated_at: new Date().toISOString(),
        name: form.name,
        description: form.description || null,
        type: form.type,
        submission_date,
        closing_date,
        image_url: form.image_url || null,
        classroom_id: form.classroom_id,
        task_template_id: task_template_id,
        content: content
      })
      .eq('id', taskId);
    
    if (error) {
      console.error('Error updating task:', error);
      throw new Error(error.message);
    }
    
    return { id: taskId };
  } catch (error) {
    console.error('Error in updateTask:', error);
    throw error;
  }
};

// Delete a task
export const deleteTask = async (taskId: string) => {
  const supabase = await createClient();
  
  // Get the current user
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }
  
  try {
    // Get the task with classroom info
    const { data: task, error: taskError } = await (supabase as any)
      .from('tasks')
      .select('*, classroom:classroom_id(*)')
      .eq('id', taskId)
      .single();
    
    if (taskError) {
      console.error('Error fetching task:', taskError);
      throw new Error('Failed to fetch task');
    }
    
    // Verify the user is the teacher of the classroom
    const classroom = task.classroom;
    if (classroom?.teacher_id !== user.id) {
      throw new Error('Unauthorized: You are not the teacher of this classroom');
    }
    
    const { error } = await (supabase as any)
      .from('tasks')
      .delete()
      .eq('id', taskId);
    
    if (error) {
      console.error('Error deleting task:', error);
      throw new Error(error.message);
    }
    
    return { success: true };
  } catch (error) {
    console.error('Error in deleteTask:', error);
    throw error;
  }
};

// Get all task templates
export const getTaskTemplates = async () => {
  const supabase = await createClient();
  
  // Get the current user
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }
  
  try {
    console.log('Fetching task templates...');
    
    // Use type assertion to avoid TypeScript errors
    const { data, error } = await (supabase as any)
      .from('task_template')
      .select('*');
    
    if (error) {
      console.error('Error fetching task templates:', error);
      throw new Error(`Failed to fetch task templates: ${error.message}`);
    }
    
    console.log(`Retrieved ${data?.length || 0} task templates:`, data);
    
    return data || [];
  } catch (error) {
    console.error('Error in getTaskTemplates:', error);
    throw error; // Re-throw to show the actual error in the UI
  }
};

// Get all classrooms where user is teacher
export const getTeacherClassrooms = async () => {
  const supabase = await createClient();
  
  // Get the current user
  const { data: { user }, error: userError } = await supabase.auth.getUser();
  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }
  
  try {
    // Get all classrooms where user is teacher
    const { data: classrooms, error: classroomsError } = await supabase
      .from('classrooms')
      .select('*')
      .eq('teacher_id', user.id)
      .eq('is_active', true)
      .order('name', { ascending: true });
    
    if (classroomsError) {
      console.error('Error fetching classrooms:', classroomsError);
      throw new Error('Failed to fetch classrooms');
    }
    
    return classrooms || [];
  } catch (error) {
    console.error('Error in getTeacherClassrooms:', error);
    throw error;
  }
};

// Create tasks table if it doesn't exist yet
export const createTasksTable = async () => {
  const supabase = await createClient();
  
  try {
    console.log('Attempting to create tasks table if needed...');
    
    // Instead of checking if the table exists (which requires pg_catalog access),
    // we'll try to query the tasks table and see if we get a "relation does not exist" error
    const { data: testTask, error: testError } = await (supabase as any)
      .from('tasks')
      .select('id')
      .limit(1);
    
    if (testError) {
      console.log('Error querying tasks table:', testError.message);
      if (testError.message.includes('does not exist')) {
        console.log('Tasks table does not exist, but we cannot create it directly');
        return { 
          success: false, 
          message: 'Tasks table does not exist. Please run the table creation SQL in the Supabase dashboard.',
          instructions: `
Create the tasks table by running this SQL in the Supabase SQL Editor:

CREATE TABLE IF NOT EXISTS public.tasks (
  id UUID PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  type TEXT NOT NULL,
  submission_date TIMESTAMP WITH TIME ZONE,
  closing_date TIMESTAMP WITH TIME ZONE,
  image_url TEXT,
  classroom_id UUID REFERENCES public.classrooms(id) ON DELETE CASCADE NOT NULL,
  task_template_id UUID REFERENCES public.task_template(id) ON DELETE SET NULL,
  content JSONB
);
`
        };
      }
      // Some other error occurred
      console.error('Unexpected error checking tasks table:', testError);
      return { success: false, message: 'Error checking tasks table: ' + testError.message };
    }
    
    // If we get here, the table exists
    console.log('Tasks table exists');
    return { success: true, message: 'Tasks table already exists' };
    
  } catch (error) {
    console.error('Error in createTasksTable:', error);
    return { 
      success: false, 
      message: 'Error checking tasks table. You may need to create it manually.',
      error: error instanceof Error ? error.message : String(error)
    };
  }
};

// Enhance the debug function
export const debugCheckSampleTask = async () => {
  const supabase = await createClient();
  
  // Sample IDs from the user's example
  const sampleTaskId = "8e173674-48cc-4e14-9192-0a97328f9ef3";
  const sampleClassroomId = "616fabfd-96d4-4555-80f4-c48a249c85fa";
  const sampleTemplateId = "c21bf474-b32f-4d11-9af7-f979dd7d7767";
  
  try {
    // Get current user
    const { data: { user }, error: userError } = await supabase.auth.getUser();
    if (userError || !user) {
      console.error('Auth error in debug function:', userError);
      return null;
    }
    console.log('Debug - Current user ID:', user.id);
    
    // Check the classroom to verify ownership
    const { data: classroom, error: classroomError } = await supabase
      .from('classrooms')
      .select('*')
      .eq('id', sampleClassroomId)
      .single();
    
    if (classroomError) {
      console.error('Error finding sample classroom:', classroomError);
    } else {
      console.log('Sample classroom:', classroom);
      console.log('Is user the teacher?', classroom.teacher_id === user.id);
    }
    
    // Check if the tasks table exists
    console.log('Checking database structure...');
    const { data: tables, error: tablesError } = await (supabase as any)
      .from('pg_tables')
      .select('tablename, schemaname')
      .eq('schemaname', 'public');
    
    if (tablesError) {
      console.error('Error listing tables:', tablesError);
    } else {
      console.log('All tables in public schema:', tables.map((t: any) => t.tablename).join(', '));
      const hasTasksTable = tables.some((table: any) => table.tablename === 'tasks');
      console.log('Does tasks table exist?', hasTasksTable);
      
      // Let's also try to query information_schema for more details
      const { data: schemaInfo, error: schemaError } = await (supabase as any)
        .from('information_schema.tables')
        .select('table_name, table_schema')
        .eq('table_schema', 'public');
      
      if (schemaError) {
        console.error('Error querying information_schema:', schemaError);
      } else {
        console.log('Tables from information_schema:', schemaInfo ? schemaInfo.map((t: any) => t.table_name).join(', ') : 'None found');
      }
    }
    
    // Direct query for the sample task
    console.log('Checking for specific sample task...');
    const { data: task, error } = await (supabase as any)
      .from('tasks')
      .select('*')
      .eq('id', sampleTaskId);
    
    if (error) {
      console.error('Error finding sample task:', error);
      if (error.message && error.message.includes('does not exist')) {
        console.log('The tasks table does not exist in the database!');
      }
    } else {
      console.log('Sample task query result:', task);
    }
    
    // Try a broader query without the specific ID
    const { data: anyTasks, error: anyTasksError } = await (supabase as any)
      .from('tasks')
      .select('count')
      .limit(1);
    
    if (anyTasksError) {
      console.error('Error checking if any tasks exist:', anyTasksError);
      if (anyTasksError.message && anyTasksError.message.includes('does not exist')) {
        console.log('The tasks table does not exist in the database!');
      }
    } else {
      console.log('Any tasks at all?', anyTasks);
    }
    
    return task;
  } catch (error) {
    console.error('Debug check error:', error);
    return null;
  }
}; 