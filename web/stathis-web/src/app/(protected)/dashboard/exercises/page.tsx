'use client';

import { useState, useEffect } from 'react';
import { Sidebar } from '@/components/dashboard/sidebar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { useRouter, useSearchParams } from 'next/navigation';
import {
  Bell,
  Search,
  Plus,
  Edit,
  Trash2,
  Filter,
  ChevronDown,
  Calendar,
  Clock,
  CheckCircle2,
  AlertCircle,
  List,
  Grid,
  X,
  Loader2,
  School,
} from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Textarea } from '@/components/ui/textarea';
import ThemeSwitcher from '@/components/theme-switcher';
import { cn } from '@/lib/utils';
import { getUserDetails, logout } from '@/services/auth';
import { 
  getTeacherTasks, 
  createTask, 
  updateTask, 
  deleteTask, 
  getTeacherClassrooms, 
  getTaskTemplates,
  debugCheckSampleTask,
  createTasksTable
} from '@/services/tasks';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { taskSchema, TaskFormValues } from '@/lib/validations/tasks';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';

// Format date for display
const formatDate = (dateString: string | null) => {
  if (!dateString) return 'No date set';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

// Find the status of a task based on dates
const getTaskStatus = (task: any) => {
  const now = new Date();
  
  if (!task.submission_date) return 'draft';
  
  const submissionDate = new Date(task.submission_date);
  const closingDate = task.closing_date ? new Date(task.closing_date) : null;
  
  if (closingDate && now > closingDate) return 'closed';
  if (now > submissionDate) return 'active';
  return 'upcoming';
};

// Task type badges
const TaskTypeBadge = ({ type }: { type: string }) => {
  let className = 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium';
  
  switch (type.toLowerCase()) {
    case 'quiz':
      className = cn(className, 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200');
      break;
    case 'assignment':
      className = cn(className, 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200');
      break;
    case 'exercise':
      className = cn(className, 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200');
      break;
    default:
      className = cn(className, 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200');
  }
  
  return <span className={className}>{type}</span>;
};

// Task status badges
const TaskStatusBadge = ({ status }: { status: string }) => {
  let className = 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium';
  
  switch (status) {
    case 'active':
      className = cn(className, 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200');
      break;
    case 'upcoming':
      className = cn(className, 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200');
      break;
    case 'closed':
      className = cn(className, 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200');
      break;
    case 'draft':
      className = cn(className, 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200');
      break;
    default:
      className = cn(className, 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200');
  }
  
  return <span className={className}>{status}</span>;
};

// Task card component
const TaskCard = ({ task, onEdit, onDelete, className }: any) => {
  const status = getTaskStatus(task);
  
  // Get classroom name from the task's classroom relation
  const classroomName = task.classroom?.name || 'Unknown Classroom';
  
  // Check if task has a template and content
  const hasTemplate = Boolean(task.task_template_id);
  const hasContent = Boolean(task.content);
  
  // Parse content if available
  let contentPreview = null;
  if (hasContent && task.content) {
    try {
      const content = typeof task.content === 'string' 
        ? JSON.parse(task.content) 
        : task.content;
      
      if (content?.title) {
        contentPreview = (
          <div className="text-xs bg-primary/10 p-2 rounded mt-2">
            <span className="font-medium">Template:</span> {content.title}
          </div>
        );
      }
    } catch (e) {
      console.error('Error parsing task content:', e);
    }
  }
  
  return (
    <Card className={cn("relative", className)}>
      <CardHeader className="pb-2">
        <div className="flex justify-between items-start">
          <div className="space-y-1">
            <CardTitle>{task.name}</CardTitle>
            <CardDescription className="line-clamp-2">
              {task.description || 'No description provided'}
            </CardDescription>
          </div>
          <TaskTypeBadge type={task.type} />
        </div>
      </CardHeader>
      <CardContent className="pb-2">
        <div className="space-y-2 text-sm">
          <div className="flex items-center text-muted-foreground">
            <Calendar className="w-4 h-4 mr-2" />
            <span>Due: {formatDate(task.submission_date)}</span>
          </div>
          {task.closing_date && (
            <div className="flex items-center text-muted-foreground">
              <Clock className="w-4 h-4 mr-2" />
              <span>Closes: {formatDate(task.closing_date)}</span>
            </div>
          )}
          <div className="flex items-center text-muted-foreground">
            <School className="w-4 h-4 mr-2" />
            <span>Class: {classroomName}</span>
          </div>
          
          {/* Display template indicator if available */}
          {contentPreview}
          {hasTemplate && !contentPreview && (
            <div className="text-xs bg-primary/10 p-2 rounded mt-2">
              <span className="font-medium">Has template</span>
            </div>
          )}
        </div>
      </CardContent>
      <CardFooter className="flex justify-between pt-2">
        <TaskStatusBadge status={status} />
        <div className="flex space-x-2">
          <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => onEdit(task)}>
            <Edit className="h-4 w-4" />
            <span className="sr-only">Edit</span>
          </Button>
          <Button variant="ghost" size="icon" className="h-8 w-8 text-destructive" onClick={() => onDelete(task)}>
            <Trash2 className="h-4 w-4" />
            <span className="sr-only">Delete</span>
          </Button>
        </div>
      </CardFooter>
    </Card>
  );
};

// Task form component for create/edit
const TaskForm = ({ task, onSubmit, isLoading }: any) => {
  const form = useForm<TaskFormValues>({
    resolver: zodResolver(taskSchema),
    defaultValues: {
      name: task?.name || '',
      description: task?.description || '',
      type: task?.type || 'exercise',
      submission_date: task?.submission_date ? new Date(task.submission_date).toISOString().substring(0, 16) : '',
      closing_date: task?.closing_date ? new Date(task.closing_date).toISOString().substring(0, 16) : '',
      classroom_id: task?.classroom_id || '',
      task_template_id: task?.task_template_id || '',
    },
  });
  
  // Get the classrooms the teacher has and task templates
  const [classrooms, setClassrooms] = useState<any[]>([]);
  const [taskTemplates, setTaskTemplates] = useState<any[]>([]);
  const [isLoadingData, setIsLoadingData] = useState(true);
  
  useEffect(() => {
    const fetchFormData = async () => {
      try {
        setIsLoadingData(true);
        
        // Fetch teacher's classrooms
        const teacherClassrooms = await getTeacherClassrooms();
        setClassrooms(teacherClassrooms);
        
        // Fetch task templates
        const templates = await getTaskTemplates();
        setTaskTemplates(templates);
        
        // If no classroom is selected and we have classrooms, auto-select the first one
        if (!form.getValues('classroom_id') && teacherClassrooms.length > 0) {
          form.setValue('classroom_id', teacherClassrooms[0].id);
        }
      } catch (error) {
        console.error('Error fetching form data:', error);
        toast.error('Failed to load form data');
      } finally {
        setIsLoadingData(false);
      }
    };
    
    fetchFormData();
  }, [form]);
  
  const handleSubmit = (values: TaskFormValues) => {
    onSubmit(values);
  };
  
  if (isLoadingData) {
    return (
      <div className="flex justify-center items-center py-8">
        <div className="flex flex-col items-center gap-2">
          <div className="animate-spin">
            <Loader2 className="h-8 w-8 text-primary" />
          </div>
          <p className="text-sm text-muted-foreground">Loading form data...</p>
        </div>
      </div>
    );
  }
  
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        <FormField
          control={form.control}
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Name</FormLabel>
              <FormControl>
                <Input placeholder="Enter task name" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        
        <FormField
          control={form.control}
          name="description"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Description</FormLabel>
              <FormControl>
                <Textarea placeholder="Enter task description" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        
        <div className="grid grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="type"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Type</FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select task type" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value="exercise">Exercise</SelectItem>
                    <SelectItem value="quiz">Quiz</SelectItem>
                    <SelectItem value="assignment">Assignment</SelectItem>
                    <SelectItem value="LESSON">Lesson</SelectItem>
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
          
          <FormField
            control={form.control}
            name="classroom_id"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Classroom</FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select classroom" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {classrooms.length === 0 ? (
                      <SelectItem value="no-classrooms" disabled>No classrooms available</SelectItem>
                    ) : (
                      classrooms.map((classroom) => (
                        <SelectItem key={classroom.id} value={classroom.id}>{classroom.name}</SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>
        
        <div className="grid grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="submission_date"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Due Date</FormLabel>
                <FormControl>
                  <Input type="datetime-local" {...field} />
                </FormControl>
                <FormDescription>When the task will be available</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
          
          <FormField
            control={form.control}
            name="closing_date"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Closing Date</FormLabel>
                <FormControl>
                  <Input type="datetime-local" {...field} />
                </FormControl>
                <FormDescription>When the task will close</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>
        
        <FormField
          control={form.control}
          name="task_template_id"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Task Template</FormLabel>
              <Select onValueChange={field.onChange} defaultValue={field.value}>
                <FormControl>
                  <SelectTrigger>
                    <SelectValue placeholder="Select task template" />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  <SelectItem value="none">None</SelectItem>
                  {taskTemplates.length === 0 ? (
                    <SelectItem value="no-templates" disabled>No templates available</SelectItem>
                  ) : (
                    taskTemplates.map((template) => {
                      // Extract content from the template
                      const content = typeof template.content === 'string' 
                        ? JSON.parse(template.content) 
                        : template.content;
                      
                      // In your sample, the title is directly in the content object
                      // Example structure: content = { pages: [...], title: "Push-Up Exercise" }
                      const templateTitle = content?.title || 'Untitled Template';
                      
                      console.log('Template:', template.id);
                      console.log('Content structure:', content);
                      
                      return (
                        <SelectItem key={template.id} value={template.id}>
                          {templateTitle}
                        </SelectItem>
                      );
                    })
                  )}
                </SelectContent>
              </Select>
              <FormDescription>Optional: Select a template to use for this task</FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
        
        <DialogFooter>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? 'Saving...' : task ? 'Update Task' : 'Create Task'}
          </Button>
        </DialogFooter>
      </form>
    </Form>
  );
};

// Main page component
export default function ExercisesPage() {
  const [userDetails, setUserDetails] = useState({
    first_name: '',
    last_name: '',
    email: ''
  });
  const [tasks, setTasks] = useState<any[]>([]);
  const [classrooms, setClassrooms] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [typeFilter, setTypeFilter] = useState('all');
  const [classroomFilter, setClassroomFilter] = useState('all');
  const [showSuccessBanner, setShowSuccessBanner] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [currentTask, setCurrentTask] = useState<any>(null);
  
  const router = useRouter();
  const searchParams = useSearchParams();
  
  useEffect(() => {
    const fetchUserAndTasks = async () => {
      try {
        setIsLoading(true);
        // Get user details
        const user = await getUserDetails();
        if (user) {
          setUserDetails({
            first_name: user.first_name,
            last_name: user.last_name,
            email: user.email
          });
        }
        
        // Get classrooms for filtering
        const teacherClassrooms = await getTeacherClassrooms();
        setClassrooms(teacherClassrooms);
        console.log('Loaded classrooms:', teacherClassrooms);
        
        // Debug check for the sample task
        const sampleTask = await debugCheckSampleTask();
        console.log('Direct check for sample task:', sampleTask);
        
        // Get tasks
        const fetchedTasks = await getTeacherTasks();
        console.log('Loaded tasks:', fetchedTasks);
        
        // Debug log if any task has template_id and content
        const tasksWithTemplates = fetchedTasks.filter((task: {task_template_id?: string}) => task.task_template_id);
        console.log('Tasks with templates:', tasksWithTemplates);
        console.log('First task content sample:', tasksWithTemplates[0]?.content);
        
        setTasks(fetchedTasks);
        
        setIsLoading(false);
      } catch (error) {
        console.error('Error loading data:', error);
        toast.error('Failed to load data');
        setIsLoading(false);
      }
    };
    
    fetchUserAndTasks();
    
    // Check for success message in URL
    const success = searchParams.get('success');
    const message = searchParams.get('message');
    
    if (success === 'true' && message) {
      setSuccessMessage(decodeURIComponent(message));
      setShowSuccessBanner(true);
      
      // Auto hide after a delay
      setTimeout(() => {
        setShowSuccessBanner(false);
      }, 5000);
      
      // Clear the URL params
      const params = new URLSearchParams(searchParams);
      params.delete('success');
      params.delete('message');
      router.replace(`?${params.toString()}`);
    }
  }, [searchParams, router]);
  
  const handleLogout = async () => {
    await logout();
  };
  
  const handleCreateTask = async (data: TaskFormValues) => {
    try {
      setIsLoading(true);
      await createTask(data);
      
      // Update tasks list
      const updatedTasks = await getTeacherTasks();
      setTasks(updatedTasks);
      
      // Close dialog and show success
      setOpenCreateDialog(false);
      toast.success('Task created successfully');
      
      // Reset loading
      setIsLoading(false);
    } catch (error) {
      console.error('Error creating task:', error);
      toast.error(error instanceof Error ? error.message : 'Failed to create task');
      setIsLoading(false);
    }
  };
  
  const handleEditTask = async (data: TaskFormValues) => {
    if (!currentTask) return;
    
    try {
      setIsLoading(true);
      await updateTask(currentTask.id, data);
      
      // Update tasks list
      const updatedTasks = await getTeacherTasks();
      setTasks(updatedTasks);
      
      // Close dialog and show success
      setOpenEditDialog(false);
      toast.success('Task updated successfully');
      
      // Reset loading and current task
      setIsLoading(false);
      setCurrentTask(null);
    } catch (error) {
      console.error('Error updating task:', error);
      toast.error(error instanceof Error ? error.message : 'Failed to update task');
      setIsLoading(false);
    }
  };
  
  const handleDeleteTask = async () => {
    if (!currentTask) return;
    
    try {
      setIsLoading(true);
      await deleteTask(currentTask.id);
      
      // Update tasks list
      const updatedTasks = await getTeacherTasks();
      setTasks(updatedTasks);
      
      // Close dialog and show success
      setOpenDeleteDialog(false);
      toast.success('Task deleted successfully');
      
      // Reset loading and current task
      setIsLoading(false);
      setCurrentTask(null);
    } catch (error) {
      console.error('Error deleting task:', error);
      toast.error(error instanceof Error ? error.message : 'Failed to delete task');
      setIsLoading(false);
    }
  };
  
  // Filter and search tasks
  const filteredTasks = tasks.filter(task => {
    const matchesSearch = searchTerm === '' || 
      task.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (task.description && task.description.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const status = getTaskStatus(task);
    const matchesStatus = statusFilter === 'all' || status === statusFilter;
    
    const matchesType = typeFilter === 'all' || task.type.toLowerCase() === typeFilter.toLowerCase();
    
    const matchesClassroom = classroomFilter === 'all' || task.classroom_id === classroomFilter;
    
    return matchesSearch && matchesStatus && matchesType && matchesClassroom;
  });
  
  // Add this function to create the sample task
  const createSampleTask = async () => {
    try {
      // First check if the tasks table exists and create it if needed
      toast.info("Checking database structure...");
      const tableResult = await createTasksTable();
      console.log("Table check result:", tableResult);
      
      if (!tableResult.success) {
        if (tableResult.instructions) {
          // Show the SQL instructions in a simple alert
          toast.error("Tasks table doesn't exist. Please create it in Supabase SQL Editor.");
          alert("Copy this SQL and run it in the Supabase SQL Editor:\n\n" + tableResult.instructions);
          return;
        }
        
        toast.error(tableResult.message || "Failed to check tasks table");
        return;
      }
      
      // Sample data with only required fields
      const sampleTask: TaskFormValues = {
        name: "Sample Exercise Task",
        description: "This is a sample exercise task to demonstrate the functionality",
        type: "exercise",
        submission_date: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
        classroom_id: "616fabfd-96d4-4555-80f4-c48a249c85fa" // Using the classroom ID from the logs
      };
      
      // Call API to create the task
      try {
        toast.info("Creating sample task...");
        const result = await createTask(sampleTask);
        console.log("Task creation result:", result);
        
        // Refresh the tasks list
        const updatedTasks = await getTeacherTasks();
        setTasks(updatedTasks);
        
        toast.success('Sample task created successfully');
      } catch (createError) {
        console.error('Error creating task:', createError);
        
        // Check if the error suggests a table does not exist
        const errorMessage = createError instanceof Error ? createError.message : String(createError);
        if (errorMessage.toLowerCase().includes('does not exist') || 
            errorMessage.toLowerCase().includes('relation') || 
            errorMessage.toLowerCase().includes('undefined table')) {
          
          toast.error("Error: Database table issue. You may need to create the tasks table.");
          alert("It appears the tasks table doesn't exist or has permission issues.\n\nPlease run this SQL in the Supabase SQL Editor:\n\nCREATE TABLE IF NOT EXISTS public.tasks (\n  id UUID PRIMARY KEY,\n  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,\n  updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,\n  name TEXT NOT NULL,\n  description TEXT,\n  type TEXT NOT NULL,\n  submission_date TIMESTAMP WITH TIME ZONE,\n  closing_date TIMESTAMP WITH TIME ZONE,\n  image_url TEXT,\n  classroom_id UUID REFERENCES public.classrooms(id) ON DELETE CASCADE NOT NULL,\n  task_template_id UUID REFERENCES public.task_template(id) ON DELETE SET NULL,\n  content JSONB\n);");
        } else {
          toast.error(errorMessage);
        }
      }
    } catch (error) {
      console.error('Error creating sample task:', error);
      toast.error(error instanceof Error ? error.message : 'Failed to create sample task');
    }
  };
  
  return (
    <div className="flex min-h-screen">
      <Sidebar className="w-64 flex-shrink-0" />
      
      <div className="flex-1 md:ml-64">
        <header className="bg-background border-b">
          <div className="flex h-16 items-center justify-end gap-4 px-4">
            <Button variant="outline" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src="/placeholder.svg" alt="User" />
                    <AvatarFallback>
                      {userDetails.first_name.charAt(0).toUpperCase()}
                      {userDetails.last_name.charAt(0).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end" forceMount>
                <DropdownMenuLabel className="font-normal">
                  <div className="flex flex-col space-y-1">
                    <p className="text-sm leading-none font-medium">
                      {userDetails.first_name} {userDetails.last_name}
                    </p>
                    <p className="text-muted-foreground text-xs leading-none">
                      {userDetails.email}
                    </p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout}>Log out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>
        
        <main className="p-6">
          {/* Success Banner */}
          {showSuccessBanner && (
            <div className={cn(
              "mb-6 flex items-center justify-between gap-4 rounded-lg border px-4 py-3 text-sm shadow-sm transition-all duration-300 ease-in-out",
              "bg-primary/5 border-primary/20 text-primary-foreground",
              "dark:bg-primary/10 dark:border-primary/30"
            )}>
              <div className="flex items-center gap-3">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10">
                  <CheckCircle2 className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <h3 className="font-semibold text-primary">Success!</h3>
                  <p className="text-muted-foreground">{successMessage}</p>
                </div>
              </div>
              <Button 
                variant="ghost" 
                size="icon" 
                className="h-8 w-8 rounded-full hover:bg-primary/10"
                onClick={() => setShowSuccessBanner(false)}
              >
                <X className="h-4 w-4 text-primary" />
                <span className="sr-only">Dismiss</span>
              </Button>
            </div>
          )}
          
          <div className="mb-6">
            <h1 className="text-2xl font-bold">Exercises & Tasks</h1>
            <p className="text-muted-foreground">Manage exercise tasks for your classes</p>
          </div>
          
          <div className="flex flex-col space-y-4 md:flex-row md:items-center md:justify-between md:space-y-0 mb-6">
            <div className="flex flex-1 items-center space-x-2 max-w-md">
              <div className="relative flex-1">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Search tasks..."
                  className="pl-8"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" size="sm" className="h-9">
                    <Filter className="mr-2 h-4 w-4" />
                    Filter
                    <ChevronDown className="ml-2 h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-[200px]">
                  <DropdownMenuLabel>Filter Tasks</DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  
                  <div className="p-2">
                    <Label className="text-xs">Status</Label>
                    <Select value={statusFilter} onValueChange={setStatusFilter}>
                      <SelectTrigger className="mt-1 h-8">
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All</SelectItem>
                        <SelectItem value="active">Active</SelectItem>
                        <SelectItem value="upcoming">Upcoming</SelectItem>
                        <SelectItem value="closed">Closed</SelectItem>
                        <SelectItem value="draft">Draft</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="p-2 pt-0">
                    <Label className="text-xs">Type</Label>
                    <Select value={typeFilter} onValueChange={setTypeFilter}>
                      <SelectTrigger className="mt-1 h-8">
                        <SelectValue placeholder="Select type" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All</SelectItem>
                        <SelectItem value="exercise">Exercise</SelectItem>
                        <SelectItem value="quiz">Quiz</SelectItem>
                        <SelectItem value="assignment">Assignment</SelectItem>
                        <SelectItem value="LESSON">Lesson</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="p-2 pt-0">
                    <Label className="text-xs">Classroom</Label>
                    <Select value={classroomFilter} onValueChange={setClassroomFilter}>
                      <SelectTrigger className="mt-1 h-8">
                        <SelectValue placeholder="Select classroom" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="all">All Classrooms</SelectItem>
                        {classrooms.map((classroom) => (
                          <SelectItem key={classroom.id} value={classroom.id}>
                            {classroom.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
            
            <div className="flex items-center space-x-2">
              <div className="hidden md:flex border rounded-md">
                <Button
                  variant={viewMode === 'grid' ? 'secondary' : 'ghost'}
                  size="sm"
                  className="h-9 rounded-r-none"
                  onClick={() => setViewMode('grid')}
                >
                  <Grid className="h-4 w-4" />
                </Button>
                <Button
                  variant={viewMode === 'list' ? 'secondary' : 'ghost'}
                  size="sm"
                  className="h-9 rounded-l-none"
                  onClick={() => setViewMode('list')}
                >
                  <List className="h-4 w-4" />
                </Button>
              </div>
              
              <Dialog open={openCreateDialog} onOpenChange={setOpenCreateDialog}>
                <DialogTrigger asChild>
                  <Button>
                    <Plus className="mr-2 h-4 w-4" /> New Task
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Create New Task</DialogTitle>
                    <DialogDescription>
                      Add details for the new task.
                    </DialogDescription>
                  </DialogHeader>
                  <TaskForm onSubmit={handleCreateTask} isLoading={isLoading} />
                </DialogContent>
              </Dialog>
              
              {/* Sample Task button for testing */}
              <Button variant="outline" onClick={createSampleTask} className="mr-2">
                Create Sample Task
              </Button>
              
              {/* Debug button */}
              <Button variant="outline" onClick={async () => {
                try {
                  toast.info("Running database diagnostics...");
                  const debug = await debugCheckSampleTask();
                  console.log("Debug complete", debug);
                  toast.success("Check browser console for debug info");
                } catch (error) {
                  console.error("Debug error:", error);
                  toast.error("Debug failed. Check console.");
                }
              }} className="bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200 hover:bg-yellow-200 dark:hover:bg-yellow-800 mr-2">
                Debug DB
              </Button>

              {/* Create Tasks Table button */}
              <Button variant="outline" onClick={async () => {
                try {
                  toast.info("Checking tasks table status...");
                  const result = await createTasksTable();
                  console.log("Table operation result:", result);
                  
                  if (!result.success) {
                    if (result.instructions) {
                      // Show the SQL instructions in a simple alert
                      toast.error("Tasks table doesn't exist. Please create it in Supabase SQL Editor.");
                      alert("Copy this SQL and run it in the Supabase SQL Editor:\n\n" + result.instructions);
                      return;
                    }
                    
                    toast.error(result.message || "Failed to check tasks table");
                    return;
                  }
                  
                  toast.success(result.message || "Table operation complete");
                  
                  // Refresh the tasks list
                  const updatedTasks = await getTeacherTasks();
                  setTasks(updatedTasks);
                } catch (error) {
                  console.error("Table creation error:", error);
                  toast.error(error instanceof Error ? error.message : "Failed to create table");
                }
              }} className="bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200 hover:bg-blue-200 dark:hover:bg-blue-800">
                Check DB Tables
              </Button>
            </div>
          </div>
          
          {/* Content tabs */}
          <Tabs defaultValue="all" className="mb-6">
            <TabsList>
              <TabsTrigger value="all">All Tasks</TabsTrigger>
              <TabsTrigger value="active">Active</TabsTrigger>
              <TabsTrigger value="upcoming">Upcoming</TabsTrigger>
              <TabsTrigger value="draft">Drafts</TabsTrigger>
            </TabsList>
          </Tabs>
          
          {/* Loading state */}
          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <div className="flex flex-col items-center gap-2">
                <div className="animate-spin">
                  <Loader2 className="h-10 w-10 text-primary" />
                </div>
                <p className="text-sm text-muted-foreground">Loading tasks...</p>
              </div>
            </div>
          ) : filteredTasks.length === 0 ? (
            <div className="flex flex-col items-center justify-center rounded-lg border border-dashed p-8 text-center">
              <div className="flex h-20 w-20 items-center justify-center rounded-full bg-muted">
                <AlertCircle className="h-10 w-10 text-muted-foreground" />
              </div>
              <h3 className="mt-4 text-lg font-semibold">No tasks found</h3>
              <p className="mt-2 text-sm text-muted-foreground">
                {searchTerm || statusFilter !== 'all' || typeFilter !== 'all' || classroomFilter !== 'all'
                  ? 'Try adjusting your search or filters'
                  : 'Get started by creating a new task'}
              </p>
              <Button className="mt-4" onClick={() => setOpenCreateDialog(true)}>
                <Plus className="mr-2 h-4 w-4" /> Create Task
              </Button>
            </div>
          ) : (
            <div className={cn(
              "grid gap-4",
              viewMode === 'grid' ? 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4' : 'grid-cols-1'
            )}>
              {filteredTasks.map((task) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  onEdit={(task: any) => {
                    setCurrentTask(task);
                    setOpenEditDialog(true);
                  }}
                  onDelete={(task: any) => {
                    setCurrentTask(task);
                    setOpenDeleteDialog(true);
                  }}
                  className={viewMode === 'list' ? 'flex flex-col md:flex-row' : ''}
                />
              ))}
            </div>
          )}
          
          {/* Edit Dialog */}
          <Dialog open={openEditDialog} onOpenChange={setOpenEditDialog}>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Edit Task</DialogTitle>
                <DialogDescription>
                  Update the task details.
                </DialogDescription>
              </DialogHeader>
              <TaskForm task={currentTask} onSubmit={handleEditTask} isLoading={isLoading} />
            </DialogContent>
          </Dialog>
          
          {/* Delete Confirmation Dialog */}
          <Dialog open={openDeleteDialog} onOpenChange={setOpenDeleteDialog}>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Delete Task</DialogTitle>
                <DialogDescription>
                  Are you sure you want to delete this task? This action cannot be undone.
                </DialogDescription>
              </DialogHeader>
              <DialogFooter>
                <Button variant="outline" onClick={() => setOpenDeleteDialog(false)}>Cancel</Button>
                <Button variant="destructive" onClick={handleDeleteTask} disabled={isLoading}>
                  {isLoading ? 'Deleting...' : 'Delete'}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </main>
      </div>
    </div>
  );
}
