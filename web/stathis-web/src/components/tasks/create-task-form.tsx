'use client';

import { useState, useEffect } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils';
import { format } from 'date-fns';
import { CalendarIcon, Loader2, Plus, Search, Eye, Trash } from 'lucide-react';
import { 
  getLessonTemplate,
  getQuizTemplate,
  getExerciseTemplate,
  deleteLessonTemplate,
  deleteQuizTemplate,
  deleteExerciseTemplate
} from '@/services/templates/api-template-client';
import { createTask } from '@/services/tasks/api-task-client';
import { TaskBodyDTO } from '@/services/tasks/api-task-client';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription
} from '@/components/ui/dialog';
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
  getTeacherLessonTemplates, 
  getTeacherQuizTemplates, 
  getTeacherExerciseTemplates 
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

export function CreateTaskForm({ classroomPhysicalId, onSuccess, onCancel }: CreateTaskFormProps): React.ReactElement {
  const [selectedTemplateType, setSelectedTemplateType] = useState<string | null>(null);
  const [selectedTemplateId, setSelectedTemplateId] = useState<string>();
  const [reviewDialogOpen, setReviewDialogOpen] = useState(false);
  const [reviewTemplateData, setReviewTemplateData] = useState<any>(null);
  
  // For delete template dialog
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [templateToDelete, setTemplateToDelete] = useState<{id: string, type: string} | null>(null);
  
  // State for lesson page navigation
  const [activePageIndex, setActivePageIndex] = useState(0);
  
  // State for quiz question navigation
  const [activeQuestionIndex, setActiveQuestionIndex] = useState(0);
  const queryClient = useQueryClient();

  // Fetch templates based on the selected type
  const { 
    data: lessonTemplates, 
    isLoading: isLoadingLessons 
  } = useQuery({
    queryKey: ['lesson-templates'],
    queryFn: () => getTeacherLessonTemplates(),
    enabled: selectedTemplateType === 'LESSON',
  });

  const { 
    data: quizTemplates, 
    isLoading: isLoadingQuizzes 
  } = useQuery({
    queryKey: ['quiz-templates'],
    queryFn: () => getTeacherQuizTemplates(),
    enabled: selectedTemplateType === 'QUIZ',
  });

  const { 
    data: exerciseTemplates, 
    isLoading: isLoadingExercises 
  } = useQuery({
    queryKey: ['exercise-templates'],
    queryFn: () => getTeacherExerciseTemplates(),
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

  // Mutation for deleting templates
  const deleteTemplateMutation = useMutation({
    mutationFn: async ({ templateId, templateType }: { templateId: string, templateType: string }) => {
      if (!templateId || !templateType) {
        throw new Error('Template ID and type are required');
      }
      
      try {
        // Call appropriate API based on template type
        let result;
        switch (templateType.toLowerCase()) {
          case 'lesson':
            result = await deleteLessonTemplate(templateId);
            break;
          case 'quiz':
            result = await deleteQuizTemplate(templateId);
            break;
          case 'exercise':
            result = await deleteExerciseTemplate(templateId);
            break;
          default:
            throw new Error('Invalid template type');
        }
        return result;
      } catch (error: any) {
        // Check if this is a permission error (403 Forbidden)
        if (error.status === 403) {
          throw new Error('You do not have permission to delete this template. Only the template creator can delete it.');
        }
        // Re-throw the original error
        throw error;
      }
    },
    onSuccess: () => {
      // Reset the template selection
      form.setValue('templatePhysicalId', '');
      setSelectedTemplateId(undefined);
      
      // Refresh the template lists
      if (selectedTemplateType === 'LESSON') {
        queryClient.invalidateQueries({ queryKey: ['lesson-templates'] });
      } else if (selectedTemplateType === 'QUIZ') {
        queryClient.invalidateQueries({ queryKey: ['quiz-templates'] });
      } else if (selectedTemplateType === 'EXERCISE') {
        queryClient.invalidateQueries({ queryKey: ['exercise-templates'] });
      }
      
      toast.success('Template deleted successfully');
    },
    onError: (error: any) => {
      // Provide a more user-friendly error message
      if (error.message?.includes('permission')) {
        toast.error(error.message);
      } else if (error.status === 403) {
        toast.error('You do not have permission to delete this template. Only the template creator can delete it.');
      } else {
        toast.error(`Error deleting template: ${error instanceof Error ? error.message : 'Unknown error'}`);
      }
    }
  });
  
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

  // Reset navigation state when template data or type changes
  useEffect(() => {
    if (reviewTemplateData) {
      setActivePageIndex(0);
      setActiveQuestionIndex(0);
    }
  }, [reviewTemplateData, selectedTemplateType]);
  
  // Helper function to render template data in a user-friendly format based on template type
  const formatTemplateData = (data: any, templateType: string) => {
    if (!data) return null;
    
    // Different display format based on template type
    switch (templateType.toLowerCase()) {
      case 'lesson':
        return formatLessonTemplate(data);
      case 'quiz':
        return formatQuizTemplate(data);
      case 'exercise':
        return formatExerciseTemplate(data);
      default:
        return (
          <div className="p-4 border rounded-md bg-muted">
            <p>Template preview not available for this type.</p>
          </div>
        );
    }
  };
  
  // Format lesson template in a user-friendly way
  const formatLessonTemplate = (data: any) => {
    console.log('Lesson template data:', data);
    
    // Handle different possible content formats
    let parsedContent: any = {};
    
    // First, try to parse the content if it's a string
    if (data.content && typeof data.content === 'string') {
      try {
        parsedContent = JSON.parse(data.content);
      } catch (e) {
        console.error('Failed to parse content string:', e);
      }
    } else if (data.content && typeof data.content === 'object') {
      // If it's already an object, use it directly
      parsedContent = data.content;
    }
    
    console.log('Parsed content:', parsedContent);
    
    // Handle pages array if it exists
    const hasPages = parsedContent.pages && Array.isArray(parsedContent.pages);
    
    return (
      <div className="space-y-6">
        <div className="space-y-3">
          <div className="space-y-1">
            <h3 className="text-base font-semibold">Title</h3>
            <p className="p-2 border rounded-md bg-background">{data.title || 'Untitled Lesson'}</p>
          </div>
          
          <div className="space-y-1">
            <h3 className="text-base font-semibold">Description</h3>
            <p className="p-2 border rounded-md bg-background min-h-[60px]">{data.description || 'No description provided'}</p>
          </div>
        </div>
        
        {/* Lesson Content Section */}
        <div className="space-y-2">
          <h3 className="text-base font-semibold">Lesson Content</h3>
          
          {/* Display pages if they exist */}
          {hasPages && parsedContent.pages.length > 0 ? (
            <div className="border rounded-md overflow-hidden">
              {/* Page Navigation Tabs */}
              <div className="flex overflow-x-auto bg-muted p-1 gap-1">
                {parsedContent.pages.map((page: any, index: number) => (
                  <button
                    key={index}
                    className={`px-3 py-1.5 text-sm font-medium rounded-md whitespace-nowrap ${activePageIndex === index ? 'bg-background shadow-sm' : 'hover:bg-background/50'}`}
                    onClick={() => setActivePageIndex(index)}
                  >
                    Page {page.pageNumber || index + 1}
                  </button>
                ))}
              </div>
              
              {/* Active Page Content */}
              <div className="p-4 bg-background">
                {activePageIndex >= 0 && activePageIndex < parsedContent.pages.length && (
                  <div className="space-y-4">
                    {/* Page Number */}
                    <div className="flex items-center justify-between border-b pb-2">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-primary/10 text-primary flex items-center justify-center font-medium text-sm">
                          {parsedContent.pages[activePageIndex].pageNumber || (activePageIndex + 1)}
                        </div>
                        {parsedContent.pages[activePageIndex].subtitle && (
                          <h3 className="text-lg font-medium">
                            {parsedContent.pages[activePageIndex].subtitle}
                          </h3>
                        )}
                      </div>
                      <div className="text-sm text-muted-foreground">
                        Page {activePageIndex + 1} of {parsedContent.pages.length}
                      </div>
                    </div>
                    
                    {/* Page Content */}
                    <div className="prose prose-sm max-w-none p-3 bg-muted/10 rounded-md my-3">
                      {parsedContent.pages[activePageIndex].paragraph ? (
                        <p>{parsedContent.pages[activePageIndex].paragraph}</p>
                      ) : parsedContent.pages[activePageIndex].content ? (
                        <p>{parsedContent.pages[activePageIndex].content}</p>
                      ) : (
                        <p className="text-muted-foreground italic">No content for this page</p>
                      )}
                    </div>
                    
                    {/* Page Media */}
                    {parsedContent.pages[activePageIndex].media && (
                      <div className="mt-4 p-3 bg-muted/50 rounded-md">
                        <div className="flex items-center gap-2">
                          <div className="w-10 h-10 rounded-md bg-muted flex items-center justify-center">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <rect width="18" height="18" x="3" y="3" rx="2" ry="2" />
                              <circle cx="9" cy="9" r="2" />
                              <path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" />
                            </svg>
                          </div>
                          <div>
                            <p className="text-sm font-medium">Media Attachment</p>
                            <p className="text-xs text-muted-foreground">{parsedContent.pages[activePageIndex].media}</p>
                          </div>
                        </div>
                      </div>
                    )}
                    
                    {/* Page Navigation Controls */}
                    <div className="flex justify-between pt-4 border-t mt-4">
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => setActivePageIndex(prev => Math.max(0, prev - 1))}
                        disabled={activePageIndex === 0}
                      >
                        Previous Page
                      </Button>
                      <div className="text-sm text-muted-foreground">
                        Page {activePageIndex + 1} of {parsedContent.pages.length}
                      </div>
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => setActivePageIndex(prev => Math.min(parsedContent.pages.length - 1, prev + 1))}
                        disabled={activePageIndex === parsedContent.pages.length - 1}
                      >
                        Next Page
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ) : parsedContent.sections ? (
            // If we have sections array
            <div className="border rounded-md p-4 bg-background">
              <div className="space-y-4">
                {parsedContent.sections.map((section: any, index: number) => (
                  <div key={index} className="p-3 border rounded-md">
                    <h4 className="font-medium mb-2">Section {index + 1}</h4>
                    <p className="text-sm">{section.text || 'No content'}</p>
                    {section.media && (
                      <div className="mt-2 p-2 bg-muted rounded-md">
                        <p className="text-xs text-muted-foreground">Media: {section.media}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ) : parsedContent.text ? (
            // If we have a single text field
            <div className="border rounded-md p-4 bg-background">
              <div className="p-3 border rounded-md">
                <h4 className="font-medium mb-2">Content</h4>
                <p className="text-sm">{parsedContent.text}</p>
                {parsedContent.media && (
                  <div className="mt-2 p-2 bg-muted rounded-md">
                    <p className="text-xs text-muted-foreground">Media: {parsedContent.media}</p>
                  </div>
                )}
              </div>
            </div>
          ) : Object.keys(parsedContent).length > 0 ? (
            // If we have any content but not in expected format
            <div className="border rounded-md p-4 bg-background">
              <div className="space-y-4">
                {Object.entries(parsedContent).map(([key, value], index) => {
                  // Handle special case for 'pages' key that's not in the expected format
                  if (key === 'pages' && Array.isArray(value)) {
                    return (
                      <div key={index} className="p-3 border rounded-md">
                        <h4 className="font-medium mb-2">Pages</h4>
                        <div className="space-y-3">
                          {value.map((page: any, pageIndex: number) => (
                            <div key={pageIndex} className="p-3 bg-muted/30 rounded-md">
                              <div className="flex items-center gap-2 mb-2">
                                <div className="w-6 h-6 rounded-full bg-muted flex items-center justify-center text-xs font-medium">
                                  {page.pageNumber || pageIndex + 1}
                                </div>
                                <h5 className="font-medium">{page.subtitle || `Page ${page.pageNumber || pageIndex + 1}`}</h5>
                              </div>
                              <p className="text-sm">{page.paragraph || page.content || 'No content'}</p>
                            </div>
                          ))}
                        </div>
                      </div>
                    );
                  }
                  
                  // Regular key-value display
                  return (
                    <div key={index} className="p-3 border rounded-md">
                      <h4 className="font-medium mb-2">{key}</h4>
                      <p className="text-sm">
                        {typeof value === 'string' ? value : JSON.stringify(value)}
                      </p>
                    </div>
                  );
                })}
              </div>
            </div>
          ) : (
            // No content
            <div className="border rounded-md p-4 bg-background">
              <p className="text-muted-foreground">No content available</p>
            </div>
          )}
        </div>
      </div>
    );
  };
  
  // Format quiz template in a user-friendly way
  const formatQuizTemplate = (data: any) => {
    console.log('Quiz template data:', data);
    
    // Handle different possible content formats
    let parsedContent: any = {};
    
    // First, try to parse the content if it's a string
    if (data.content && typeof data.content === 'string') {
      try {
        parsedContent = JSON.parse(data.content);
      } catch (e) {
        console.error('Failed to parse content string:', e);
      }
    } else if (data.content && typeof data.content === 'object') {
      // If it's already an object, use it directly
      parsedContent = data.content;
    }
    
    console.log('Parsed quiz content:', parsedContent);
    
    // Determine if we have questions
    const questions = parsedContent.questions || (Array.isArray(parsedContent) ? parsedContent : []);
    const hasQuestions = questions.length > 0;
    
    return (
      <div className="space-y-6">
        <div className="space-y-3">
          <div className="space-y-1">
            <h3 className="text-base font-semibold">Title</h3>
            <p className="p-2 border rounded-md bg-background">{data.title || 'Untitled Quiz'}</p>
          </div>
          
          <div className="space-y-1">
            <h3 className="text-base font-semibold">Instructions</h3>
            <p className="p-2 border rounded-md bg-background min-h-[60px]">{data.instruction || 'No instructions provided'}</p>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <h3 className="text-base font-semibold">Max Score</h3>
              <p className="p-2 border rounded-md bg-background">{data.maxScore || '0'} points</p>
            </div>
          </div>
        </div>
        
        <div className="space-y-2">
          <h3 className="text-base font-semibold">Quiz Questions</h3>
          
          {hasQuestions ? (
            <div className="border rounded-md overflow-hidden">
              {/* Question Navigation */}
              <div className="bg-muted p-2 flex gap-1 overflow-x-auto">
                {questions.map((question: any, index: number) => (
                  <button
                    key={index}
                    onClick={() => setActiveQuestionIndex(index)}
                    className={`px-2 py-1 min-w-[32px] text-sm font-medium rounded ${activeQuestionIndex === index ? 'bg-background shadow-sm' : 'hover:bg-background/50'}`}
                  >
                    {index + 1}
                  </button>
                ))}
              </div>
              
              {/* Active Question Display */}
              <div className="p-4 bg-background">
                {activeQuestionIndex >= 0 && activeQuestionIndex < questions.length && (
                  <div className="space-y-4">
                    {/* Question Header */}
                    <div className="pb-2 border-b">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-primary/10 text-primary flex items-center justify-center font-medium text-sm">
                          {activeQuestionIndex + 1}
                        </div>
                        <h3 className="text-lg font-medium">Question {activeQuestionIndex + 1}</h3>
                      </div>
                    </div>
                    
                    {/* Question Text */}
                    <div className="p-3 bg-muted/20 rounded-md">
                      <p className="font-medium">{questions[activeQuestionIndex].question || questions[activeQuestionIndex].text || 'No question text'}</p>
                    </div>
                    
                    {/* Question Options */}
                    {questions[activeQuestionIndex].options && (
                      <div className="space-y-3">
                        <h4 className="text-base font-medium">Answer Options</h4>
                        <div className="space-y-2 pl-1">
                          {/* Handle both array of strings and array of objects */}
                          {Array.isArray(questions[activeQuestionIndex].options) && (
                            questions[activeQuestionIndex].options.map((option: any, optIndex: number) => {
                              // Check if options is an array of strings or objects
                              const isString = typeof option === 'string';
                              const optionText = isString ? option : option.text || `Option ${optIndex + 1}`;
                              
                              // Determine if this option is correct
                              let isCorrect = false;
                              if (questions[activeQuestionIndex].answer !== undefined) {
                                // If there's an 'answer' field with an index
                                isCorrect = optIndex === questions[activeQuestionIndex].answer;
                              } else if (!isString && option.isCorrect !== undefined) {
                                // If each option has an isCorrect property
                                isCorrect = option.isCorrect;
                              }
                              
                              return (
                                <div 
                                  key={optIndex} 
                                  className={`flex items-center p-2 rounded-md border ${isCorrect ? 'border-green-200 bg-green-50 dark:bg-green-900/20 dark:border-green-900' : 'border-transparent'}`}
                                >
                                  <div className={`w-5 h-5 rounded-full border flex items-center justify-center mr-3 ${isCorrect ? 'bg-green-500 border-green-600 text-white' : 'border-gray-300'}`}>
                                    {isCorrect && (
                                      <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                                        <polyline points="20 6 9 17 4 12" />
                                      </svg>
                                    )}
                                  </div>
                                  <span className={isCorrect ? 'font-medium' : ''}>
                                    {optionText}
                                  </span>
                                </div>
                              );
                            })
                          )}
                        </div>
                      </div>
                    )}
                    
                    {/* Navigation Controls */}
                    <div className="flex justify-between pt-4 border-t mt-4">
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => setActiveQuestionIndex(prev => Math.max(0, prev - 1))}
                        disabled={activeQuestionIndex === 0}
                      >
                        Previous Question
                      </Button>
                      <div className="text-sm text-muted-foreground self-center">
                        Question {activeQuestionIndex + 1} of {questions.length}
                      </div>
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => setActiveQuestionIndex(prev => Math.min(questions.length - 1, prev + 1))}
                        disabled={activeQuestionIndex === questions.length - 1}
                      >
                        Next Question
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ) : Object.keys(parsedContent).length > 0 ? (
            // If we have any content but not in expected format
            <div className="border rounded-md p-4 bg-background">
              <div className="space-y-4">
                {Object.entries(parsedContent).map(([key, value], index) => (
                  <div key={index} className="p-3 border rounded-md">
                    <h4 className="font-medium mb-2">{key}</h4>
                    <p className="text-sm">
                      {typeof value === 'string' ? value : JSON.stringify(value, null, 2)}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="border rounded-md p-4 bg-background">
              <p className="text-muted-foreground">No questions available</p>
            </div>
          )}
        </div>
      </div>
    );
  };
  
  // Format exercise template in a user-friendly way
  const formatExerciseTemplate = (data: any) => {
    console.log('Exercise template data:', data);
    
    // Handle different possible content formats
    let parsedContent: any = {};
    
    // First, try to parse the content if it's a string
    if (data.content && typeof data.content === 'string') {
      try {
        parsedContent = JSON.parse(data.content);
      } catch (e) {
        console.error('Failed to parse content string:', e);
      }
    } else if (data.content && typeof data.content === 'object') {
      // If it's already an object, use it directly
      parsedContent = data.content;
    } else {
      // If no content object, use the data itself
      parsedContent = data;
    }
    
    console.log('Parsed exercise content:', parsedContent);
    
    // Extract exercise information from any possible data structure
    const exerciseType = parsedContent.type || parsedContent.exerciseType || data.type || 'Not specified';
    const goalReps = parsedContent.goalReps || parsedContent.reps || parsedContent.repetitions || 'N/A';
    
    // Handle accuracy that might be stored as decimal (0.8) or percentage (80)
    let accuracyValue = parsedContent.goalAccuracy || parsedContent.accuracy || parsedContent.accuracyGoal;
    let accuracyDisplay = 'N/A';
    if (accuracyValue !== undefined) {
      // If it's a decimal (less than 1), convert to percentage
      if (typeof accuracyValue === 'number' && accuracyValue <= 1) {
        accuracyDisplay = `${(accuracyValue * 100).toFixed(0)}%`;
      } else {
        // If it's already a percentage or string
        accuracyDisplay = typeof accuracyValue === 'number' ? `${accuracyValue}%` : accuracyValue;
      }
    }
    
    // Handle time that might be stored in different formats
    const timeValue = parsedContent.goalTime || parsedContent.time || parsedContent.timeGoal;
    let timeDisplay = 'N/A';
    if (timeValue !== undefined) {
      timeDisplay = `${timeValue} sec`;
    }
    
    // Get difficulty if available
    const difficulty = parsedContent.difficulty || data.difficulty || parsedContent.difficultyLevel;
    
    // Get exercise name/title if available
    const exerciseName = parsedContent.name || parsedContent.title || parsedContent.exerciseName || data.name || '';
    
    return (
      <div className="space-y-6">
        <div className="rounded-lg border overflow-hidden">
          <div className="bg-muted p-4">
            <h3 className="text-lg font-medium">
              {exerciseName ? exerciseName : 'Exercise Information'}
            </h3>
          </div>
          <div className="p-4 space-y-4">
            {/* Exercise Type */}
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-md bg-primary/10 text-primary flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-activity">
                  <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
                </svg>
              </div>
              <div>
                <h4 className="text-sm font-medium">Exercise Type</h4>
                <p className="text-base">{exerciseType}</p>
              </div>
            </div>
            
            {/* Exercise Goals */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-4">
              {/* Repetitions */}
              <div className="p-3 rounded-md bg-background border">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-blue-50 text-blue-500 flex items-center justify-center dark:bg-blue-900/20 dark:text-blue-400">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-repeat">
                      <path d="m17 2 4 4-4 4" />
                      <path d="M3 11v-1a4 4 0 0 1 4-4h14" />
                      <path d="m7 22-4-4 4-4" />
                      <path d="M21 13v1a4 4 0 0 1-4 4H3" />
                    </svg>
                  </div>
                  <h4 className="text-sm font-medium">Goal Reps</h4>
                </div>
                <p className="text-2xl font-semibold mt-2">{goalReps}</p>
              </div>
              
              {/* Accuracy */}
              <div className="p-3 rounded-md bg-background border">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-green-50 text-green-500 flex items-center justify-center dark:bg-green-900/20 dark:text-green-400">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-check-circle">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                      <polyline points="22 4 12 14.01 9 11.01" />
                    </svg>
                  </div>
                  <h4 className="text-sm font-medium">Goal Accuracy</h4>
                </div>
                <p className="text-2xl font-semibold mt-2">{accuracyDisplay}</p>
              </div>
              
              {/* Time */}
              <div className="p-3 rounded-md bg-background border">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-yellow-50 text-yellow-500 flex items-center justify-center dark:bg-yellow-900/20 dark:text-yellow-400">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-timer">
                      <path d="M10 2h4" />
                      <path d="M12 14v-4" />
                      <path d="M12 14v-4" />
                      <circle cx="12" cy="14" r="8" />
                    </svg>
                  </div>
                  <h4 className="text-sm font-medium">Goal Time</h4>
                </div>
                <p className="text-2xl font-semibold mt-2">{timeDisplay}</p>
              </div>
            </div>
            
            {/* Exercise Difficulty */}
            {difficulty && (
              <div className="mt-4 p-3 rounded-md bg-muted/50">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-gauge">
                      <path d="m12 15 3.5-3.5" />
                      <path d="M20.3 18c.4-1 .7-2.2.7-3.4C21 9.8 17 6 12 6s-9 3.8-9 8.6c0 1.2.3 2.4.7 3.4" />
                    </svg>
                  </div>
                  <div>
                    <h4 className="text-sm font-medium">Difficulty Level</h4>
                    <p className="text-base capitalize">{difficulty}</p>
                  </div>
                </div>
              </div>
            )}
            
            {/* Any exercise description if available */}
            {(parsedContent.description || parsedContent.instructions) && (
              <div className="mt-4 p-3 rounded-md bg-muted/30 border">
                <h4 className="text-sm font-medium mb-2">Instructions</h4>
                <p className="text-sm">{parsedContent.description || parsedContent.instructions}</p>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };
  
  // Helper functions for formatting exercise data
  const formatExerciseType = (type: string) => {
    const typeMap: Record<string, string> = {
      'PUSH_UP': 'Push Up',
      'SIT_UP': 'Sit Up',
      'JUMPING_JACK': 'Jumping Jack',
      'TYPE1': 'Type 1',
      'TYPE2': 'Type 2'
    };
    return typeMap[type] || type;
  };
  
  const formatDifficulty = (difficulty: string) => {
    const difficultyMap: Record<string, string> = {
      'BEGINNER': 'Beginner',
      'INTERMEDIATE': 'Intermediate',
      'ADVANCED': 'Advanced'
    };
    return difficultyMap[difficulty] || difficulty;
  };
  
  const formatTime = (seconds: string) => {
    if (!seconds) return 'Not specified';
    
    const timeMap: Record<string, string> = {
      '30': '30 seconds',
      '60': '1 minute',
      '90': '1.5 minutes',
      '120': '2 minutes'
    };
    return timeMap[seconds] || `${seconds} seconds`;
  };

  return (
    <>
      <Dialog open={reviewDialogOpen} onOpenChange={setReviewDialogOpen}>
        <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              Template Review: {reviewTemplateData?.title || 'Template'}
            </DialogTitle>
            <DialogDescription>
              Reviewing {selectedTemplateType?.toLowerCase()} template details
            </DialogDescription>
          </DialogHeader>
          
          <div className="mt-4">
            {formatTemplateData(reviewTemplateData, selectedTemplateType?.toLowerCase() || '')}
          </div>
        </DialogContent>
      </Dialog>

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
                  <div className="flex space-x-2">
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
                    <Button 
                      type="button" /* Explicitly set button type to prevent form submission */
                      variant="ghost" 
                      size="sm" 
                      className="h-8 px-2"
                      onClick={async () => {
                        if (selectedTemplateId && selectedTemplateType) {
                          const templateType = selectedTemplateType.toLowerCase() as 'lesson' | 'quiz' | 'exercise';
                          try {
                            let data;
                            
                            // Call appropriate API based on template type
                            switch (templateType) {
                              case 'lesson':
                                data = await getLessonTemplate(selectedTemplateId);
                                break;
                              case 'quiz':
                                data = await getQuizTemplate(selectedTemplateId);
                                break;
                              case 'exercise':
                                data = await getExerciseTemplate(selectedTemplateId);
                                break;
                              default:
                                toast.error('Invalid template type');
                                return;
                            }
                            
                            // Store template data and open dialog
                            setReviewTemplateData(data);
                            setReviewDialogOpen(true);
                          } catch (error) {
                            toast.error('Failed to load template');
                            console.error('Error fetching template:', error);
                          }
                        } else {
                          toast.error('Please select a template first');
                        }
                      }}
                      disabled={!selectedTemplateId || !selectedTemplateType}
                    >
                      <Eye className="h-4 w-4 mr-1" />
                      Review Template
                    </Button>
                    <Button 
                      type="button"
                      variant="ghost" 
                      size="sm" 
                      className="h-8 px-2 text-destructive hover:bg-destructive/10"
                      onClick={() => {
                        if (selectedTemplateId && selectedTemplateType) {
                          setTemplateToDelete({
                            id: selectedTemplateId,
                            type: selectedTemplateType
                          });
                          setDeleteDialogOpen(true);
                        } else {
                          toast.error('Please select a template first');
                        }
                      }}
                      disabled={!selectedTemplateId || !selectedTemplateType}
                    >
                      <Trash className="h-4 w-4 mr-1" />
                      Delete Template
                    </Button>
                  </div>
                </div>
                <Select 
                  onValueChange={(value) => {
                    if (value === 'create-new') {
                      // This is handled in the SelectItem click now with the modal
                      return;
                    }
                    field.onChange(value);
                    // Store selected template ID for review
                    setSelectedTemplateId(value);
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

    {/* Delete Template Confirmation Dialog */}
    <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Are you sure?</AlertDialogTitle>
          <AlertDialogDescription>
            This action cannot be undone. This will permanently delete the {templateToDelete?.type?.toLowerCase()} template.
            <br />
            <br />
            <strong>Note:</strong> You can only delete templates that you created yourself.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancel</AlertDialogCancel>
          <AlertDialogAction 
            onClick={() => {
              if (templateToDelete) {
                deleteTemplateMutation.mutate({ 
                  templateId: templateToDelete.id, 
                  templateType: templateToDelete.type 
                });
                // Close the dialog after successful deletion
                setDeleteDialogOpen(false);
              }
            }}
            className="bg-red-500 hover:bg-red-600"
          >
            {deleteTemplateMutation.isPending ? (
              <span className="flex items-center">
                <span className="mr-1 h-3 w-3 animate-spin rounded-full border-2 border-r-transparent"></span>
                Deleting...
              </span>
            ) : 'Delete'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </>
  );
}
