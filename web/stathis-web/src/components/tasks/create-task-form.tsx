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
import { CalendarIcon, Loader2 } from 'lucide-react';
import { createTask } from '@/services/tasks/api-task-client';
import { TaskBodyDTO } from '@/services/tasks/api-task-client';
import { useMutation, useQuery } from '@tanstack/react-query';
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
} from "@/components/ui/select";
import { toast } from 'sonner';
import { 
  getAllLessonTemplates, 
  getAllQuizTemplates, 
  getAllExerciseTemplates 
} from '@/services/templates/api-template-client';

// Task form schema
const taskFormSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters').max(100, 'Title cannot exceed 100 characters'),
  description: z.string().min(3, 'Description must be at least 3 characters').max(500, 'Description cannot exceed 500 characters'),
  dueDate: z.date({
    required_error: "Please select a due date",
  }),
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

  const createTaskMutation = useMutation({
    mutationFn: (data: TaskFormValues) => {
      // Create the task DTO
      const taskData: TaskBodyDTO = {
        title: data.title,
        description: data.description,
        classroomPhysicalId,
        dueDate: data.dueDate.toISOString(),
        templatePhysicalId: data.templatePhysicalId,
        templateType: data.templateType,
        points: data.points
      };
      
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
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
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
                <FormLabel>Points</FormLabel>
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
                  Maximum points students can earn
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
                  <SelectTrigger>
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
                <FormLabel>Template</FormLabel>
                <Select 
                  onValueChange={field.onChange}
                  defaultValue={field.value}
                  disabled={isLoadingTemplates()}
                >
                  <FormControl>
                    <SelectTrigger>
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
                      getAvailableTemplates().map((template: any) => (
                        <SelectItem key={template.physicalId} value={template.physicalId}>
                          {template.title}
                        </SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
                <FormDescription>
                  {selectedTemplateType === 'LESSON' && 'Select a lesson to assign'}
                  {selectedTemplateType === 'QUIZ' && 'Select a quiz to assign'}
                  {selectedTemplateType === 'EXERCISE' && 'Select an exercise to assign'}
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
        )}

        <div className="flex justify-end gap-2 pt-4">
          <Button type="button" variant="outline" onClick={onCancel}>
            Cancel
          </Button>
          <Button 
            type="submit" 
            disabled={createTaskMutation.isPending || isLoadingTemplates()}
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
