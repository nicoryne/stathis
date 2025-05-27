'use client';

import { useState, useEffect } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils';
import { format } from 'date-fns';
import { CalendarIcon, Loader2, Plus } from 'lucide-react';
import { createTask } from '@/services/tasks/api-task-client';
import { TaskBodyDTO } from '@/services/tasks/api-task-client';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  FormDescription
} from '@/components/ui/form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  SelectSeparator,
  SelectGroup,
  SelectLabel
} from "@/components/ui/select";
import { toast } from 'sonner';
import { 
  getAllLessonTemplates, 
  getAllQuizTemplates, 
  getAllExerciseTemplates 
} from '@/services/templates/api-template-client';
import { TemplateCreationModal } from '@/components/templates/template-creation-modal';

// Task form schema
const taskFormSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters').max(100, 'Title cannot exceed 100 characters'),
  description: z.string().min(3, 'Description must be at least 3 characters').max(500, 'Description cannot exceed 500 characters'),
  dueDate: z.date({
    required_error: "Please select a due date",
  }).refine(
    (date) => {
      // Get current date with time set to start of day for fair comparison
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return date >= today;
    },
    {
      message: "Due date cannot be in the past"
    }
  ),
  templateType: z.enum(['LESSON', 'QUIZ', 'EXERCISE'], {
    required_error: "Please select a template type",
  }),
  templatePhysicalId: z.string({
    required_error: "Please select a template",
  }),
  points: z.number().min(0, 'Points must be a positive number').max(100, 'Points cannot exceed 100'),
});

// Form values type
type TaskFormValues = z.infer<typeof taskFormSchema>;

interface CreateTaskFormProps {
  classroomPhysicalId: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export function CreateTaskForm({ classroomPhysicalId, onSuccess, onCancel }: CreateTaskFormProps) {
  const [selectedTemplateType, setSelectedTemplateType] = useState<string | null>(null);
  const queryClient = useQueryClient();

  // Fetch templates based on the selected type
  const { 
    data: lessonTemplates, 
    isLoading: isLoadingLessons 
  } = useQuery({
    queryKey: ['lesson-templates'],
    queryFn: () => getAllLessonTemplates(),
    enabled: selectedTemplateType === 'LESSON',
  });

  const { 
    data: quizTemplates, 
    isLoading: isLoadingQuizzes 
  } = useQuery({
    queryKey: ['quiz-templates'],
    queryFn: () => getAllQuizTemplates(),
    enabled: selectedTemplateType === 'QUIZ',
  });

  const { 
    data: exerciseTemplates, 
    isLoading: isLoadingExercises 
  } = useQuery({
    queryKey: ['exercise-templates'],
    queryFn: () => getAllExerciseTemplates(),
    enabled: selectedTemplateType === 'EXERCISE',
  });

  const form = useForm<TaskFormValues>({
    resolver: zodResolver(taskFormSchema),
    defaultValues: {
      title: '',
      description: '',
      points: 10,
    }
  });

  // Update form value when template type changes
  useEffect(() => {
    if (selectedTemplateType) {
      form.setValue('templateType', selectedTemplateType as 'LESSON' | 'QUIZ' | 'EXERCISE');
      form.setValue('templatePhysicalId', ''); // Reset template selection
    }
  }, [selectedTemplateType, form]);

  const handleTemplateTypeChange = (value: string) => {
    setSelectedTemplateType(value);
  };
  
  const handleTemplateCreated = () => {
    // Invalidate relevant template queries when a new template is created
    if (selectedTemplateType === 'LESSON') {
      queryClient.invalidateQueries({ queryKey: ['lesson-templates'] });
    } else if (selectedTemplateType === 'QUIZ') {
      queryClient.invalidateQueries({ queryKey: ['quiz-templates'] });
    } else if (selectedTemplateType === 'EXERCISE') {
      queryClient.invalidateQueries({ queryKey: ['exercise-templates'] });
    }
    toast.success('Template created successfully');
  };

  const createTaskMutation = useMutation({
    mutationFn: (data: TaskFormValues) => {
      // Ensure classroom ID follows the required pattern: [A-Z0-9-]+
      // Convert to uppercase if needed
      const formattedClassroomId = classroomPhysicalId.toUpperCase();
      
      // Format dates according to API specification, ensuring proper ISO format
      // The spec requires: ^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}([+-]\d{2}:\d{2}|Z)$
      const dueDate = data.dueDate.toISOString();
      // Trim milliseconds if present to match exact pattern
      const formattedDueDate = dueDate.replace(/\.\d{3}/, '');
      
      // Create task data with strict adherence to schema requirements
      const taskData: TaskBodyDTO = {
        name: data.title,
        description: data.description || '',
        submissionDate: formattedDueDate,
        closingDate: formattedDueDate,
        classroomPhysicalId: formattedClassroomId
      };
      
      // Set the appropriate template ID based on selected type
      // Ensuring they match the exact required patterns
      if (data.templateType === 'EXERCISE') {
        // Pattern must be: ^EXERCISE-[A-Z0-9-]+$
        // Ensure it starts with EXERCISE- prefix
        let exerciseId = data.templatePhysicalId;
        if (!exerciseId.startsWith('EXERCISE-')) {
          exerciseId = exerciseId.includes('EXERCISE-') 
            ? exerciseId 
            : `EXERCISE-${exerciseId}`;
        }
        taskData.exerciseTemplateId = exerciseId.toUpperCase();
      } else if (data.templateType === 'LESSON') {
        // Pattern must be: ^LESSON-[A-Z0-9-]+$
        // Ensure it starts with LESSON- prefix
        let lessonId = data.templatePhysicalId;
        if (!lessonId.startsWith('LESSON-')) {
          lessonId = lessonId.includes('LESSON-') 
            ? lessonId 
            : `LESSON-${lessonId}`;
        }
        taskData.lessonTemplateId = lessonId.toUpperCase();
      } else if (data.templateType === 'QUIZ') {
        // Pattern must be: ^[A-Za-z0-9-]+$
        // This is already satisfied by any alphanumeric ID
        taskData.quizTemplateId = data.templatePhysicalId;
      }
      
      // Add maxAttempts if points are specified
      if (data.points !== undefined && data.points !== null) {
        taskData.maxAttempts = data.points;
      }
      
      console.log('Sending task data to API with strict validation:', taskData);
      return createTask(taskData);
    },
    onSuccess: () => {
      toast.success('Task created successfully');
      onSuccess();
      form.reset();
    },
    onError: (error) => {
      toast.error(`Error creating task: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });

  const onSubmit = (values: TaskFormValues) => {
    // Check if the user selected the "create new template" option
    if (values.templatePhysicalId === 'create-new') {
      toast.error('Please create and select a template first');
      return;
    }
    createTaskMutation.mutate(values);
  };

  // Helper function to get available templates based on selected type
  const getAvailableTemplates = () => {
    if (selectedTemplateType === 'LESSON') {
      return lessonTemplates || [];
    } else if (selectedTemplateType === 'QUIZ') {
      return quizTemplates || [];
    } else if (selectedTemplateType === 'EXERCISE') {
      return exerciseTemplates || [];
    }
    return [];
  };

  // Helper function to check if templates are loading
  const isLoadingTemplates = () => {
    if (selectedTemplateType === 'LESSON') {
      return isLoadingLessons;
    } else if (selectedTemplateType === 'QUIZ') {
      return isLoadingQuizzes;
    } else if (selectedTemplateType === 'EXERCISE') {
      return isLoadingExercises;
    }
    return false;
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="flex flex-col h-full max-h-[70vh] min-h-[500px] overflow-hidden bg-background rounded-lg shadow-sm border border-border">
        <div className="flex-1 overflow-y-auto px-5 pt-5 space-y-5">
        <FormField
          control={form.control}
          name="title"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Title</FormLabel>
              <FormControl>
                <Input placeholder="Week 1 Assignment" {...field} />
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
                <Textarea 
                  placeholder="Task instructions and details..." 
                  className="min-h-[100px]"
                  {...field} 
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="dueDate"
            render={({ field }) => (
              <FormItem className="flex flex-col">
                <FormLabel>Due Date</FormLabel>
                <Popover>
                  <PopoverTrigger asChild>
                    <FormControl>
                      <Button
                        variant={"outline"}
                        className={cn(
                          "w-full pl-3 text-left font-normal",
                          !field.value && "text-muted-foreground"
                        )}
                      >
                        {field.value ? (
                          format(field.value, "PPP")
                        ) : (
                          <span>Pick a date</span>
                        )}
                        <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                      </Button>
                    </FormControl>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      date={field.value}
                      onDateChange={field.onChange}
                      min={new Date()} // Prevent selecting dates in the past
                    />
                  </PopoverContent>
                </Popover>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="points"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Max Attempts</FormLabel>
                <FormControl>
                  <Input 
                    type="number" 
                    min={0} 
                    max={100} 
                    placeholder="10" 
                    {...field}
                    onChange={(e) => field.onChange(Number(e.target.value))}
                  />
                </FormControl>
                <FormDescription>
                  Maximum number of attempts allowed for this task
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="templateType"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Template Type</FormLabel>
              <Select 
                onValueChange={(value) => {
                  field.onChange(value);
                  handleTemplateTypeChange(value);
                }}
                defaultValue={field.value}
              >
                <FormControl>
                  <SelectTrigger className="w-full bg-background">
                    <SelectValue placeholder="Select template type" />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  <SelectItem value="LESSON">Lesson</SelectItem>
                  <SelectItem value="QUIZ">Quiz</SelectItem>
                  <SelectItem value="EXERCISE">Exercise</SelectItem>
                </SelectContent>
              </Select>
              <FormDescription>
                Type of content to assign to students
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />

        {selectedTemplateType && (
          <FormField
            control={form.control}
            name="templatePhysicalId"
            render={({ field }) => (
              <FormItem>
                <div className="flex justify-between items-center">
                  <FormLabel>Template</FormLabel>
                  <TemplateCreationModal 
                    templateType={selectedTemplateType as 'LESSON' | 'QUIZ' | 'EXERCISE'} 
                    onTemplateCreated={handleTemplateCreated}
                    continueToTask={true} /* Set to true to keep the dialog open for task creation */
                    trigger={
                      <Button variant="ghost" size="sm" className="h-8 px-2">
                        <Plus className="h-4 w-4 mr-1" />
                        New Template
                      </Button>
                    }
                  />
                </div>
                <Select 
                  onValueChange={(value) => {
                    if (value === 'create-new') {
                      // This is handled in the SelectItem click now with the modal
                      return;
                    }
                    field.onChange(value);
                  }}
                  value={field.value}
                  disabled={isLoadingTemplates()}
                >
                  <FormControl>
                    <SelectTrigger className="w-full bg-background">
                      {isLoadingTemplates() ? (
                        <div className="flex items-center">
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Loading templates...
                        </div>
                      ) : (
                        <SelectValue placeholder="Select a template" />
                      )}
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {getAvailableTemplates().length === 0 ? (
                      <div className="p-2 text-sm text-muted-foreground">
                        No templates available. Please create a template first.
                      </div>
                    ) : (
                      <>
                        <SelectGroup>
                          <SelectLabel>Available Templates</SelectLabel>
                          {getAvailableTemplates().map((template: any) => (
                            <SelectItem key={template.physicalId} value={template.physicalId}>
                              {template.title}
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </>
                    )}
                    <SelectSeparator />
                  </SelectContent>
                </Select>
                <FormDescription className="mt-1 text-muted-foreground">
                  {selectedTemplateType === 'LESSON' && 'Select a lesson to assign to students'}
                  {selectedTemplateType === 'QUIZ' && 'Select a quiz to assign to students'}
                  {selectedTemplateType === 'EXERCISE' && 'Select an exercise to assign to students'}
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
        )}
        </div>

        <div className="flex justify-end gap-3 p-5 pt-4 mt-5 border-t border-border">
          <Button 
            type="button" 
            variant="outline" 
            onClick={onCancel}
            className="px-5"
          >
            Cancel
          </Button>
          <Button 
            type="submit" 
            disabled={createTaskMutation.isPending}
            className="px-5"
          >
            {createTaskMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Creating...
              </>
            ) : (
              'Create Task'
            )}
          </Button>
        </div>
      </form>
    </Form>
  );
}
