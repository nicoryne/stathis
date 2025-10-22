'use client';

import { useState, useEffect, useMemo } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { motion } from 'framer-motion';
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
import { CalendarIcon, Clock, Loader2, Plus, Search, Eye, Trash } from 'lucide-react';
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
  dueTime: z.string().regex(/^([01]\d|2[0-3]):([0-5]\d)$/, 'Invalid time format (HH:MM)'),
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
  onSwitchToTemplate?: (formData: any) => void;
}

export function CreateTaskForm({ classroomPhysicalId, onSuccess, onCancel, onSwitchToTemplate }: CreateTaskFormProps): React.ReactElement {
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

  // Memoize the minimum date to prevent Calendar re-renders
  const minDate = useMemo(() => new Date(), []);

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

  // Default form values
  const form = useForm<TaskFormValues>({
    resolver: zodResolver(taskFormSchema),
    defaultValues: {
      title: '',
      description: '',
      points: 3,
      dueTime: '23:59',
      templateType: undefined,
      templatePhysicalId: undefined,
    },
  });

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
        if (error?.status === 403) {
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
      if (error?.message?.includes('permission')) {
        toast.error(error.message);
      } else if (error?.status === 403) {
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
      // Combine date and time
      const [hours, minutes] = data.dueTime.split(':').map(Number);
      const combinedDate = new Date(data.dueDate);
      combinedDate.setHours(hours, minutes, 0, 0);
      
      const dueDate = combinedDate.toISOString();
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

  // Reset navigation state when template data changes
  useEffect(() => {
    if (reviewTemplateData) {
      setActivePageIndex(0);
      setActiveQuestionIndex(0);
    }
  }, [reviewTemplateData]);
  
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
      <div className="space-y-8">
        {/* Header Section */}
        <div className="bg-gradient-to-r from-primary/5 to-secondary/5 backdrop-blur-sm rounded-2xl p-6 border border-border/20">
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center">
                <span className="text-lg">📚</span>
              </div>
              <div>
                <h3 className="text-xl font-bold text-foreground">Lesson Information</h3>
                <p className="text-sm text-muted-foreground">Template details and content</p>
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Title</label>
                <div className="p-3 bg-background/60 backdrop-blur-sm rounded-xl border border-border/30">
                  <p className="font-medium">{data.title || 'Untitled Lesson'}</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Description</label>
                <div className="p-3 bg-background/60 backdrop-blur-sm rounded-xl border border-border/30 min-h-[60px]">
                  <p className="text-sm">{data.description || 'No description provided'}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Lesson Content Section */}
        <div className="space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-gradient-to-r from-secondary/20 to-accent/20 flex items-center justify-center">
              <span className="text-sm">📖</span>
            </div>
            <h3 className="text-lg font-semibold">Lesson Content</h3>
          </div>
          
          {/* Display pages if they exist */}
          {hasPages && parsedContent.pages.length > 0 ? (
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 overflow-hidden shadow-lg">
              {/* Page Navigation Tabs */}
              <div className="bg-muted/40 backdrop-blur-sm p-2 border-b border-border/20">
                <div className="flex overflow-x-auto gap-2">
                  {parsedContent.pages.map((page: any, index: number) => (
                    <motion.button
                      key={index}
                      whileHover={{ scale: 1.02 }}
                      whileTap={{ scale: 0.98 }}
                      className={`px-4 py-2 text-sm font-medium rounded-xl whitespace-nowrap transition-all duration-200 ${
                        activePageIndex === index 
                          ? 'bg-gradient-to-r from-primary to-secondary text-white shadow-lg' 
                          : 'bg-background/60 hover:bg-background/80 text-muted-foreground'
                      }`}
                      onClick={() => setActivePageIndex(index)}
                    >
                      Page {page.pageNumber || index + 1}
                    </motion.button>
                  ))}
                </div>
              </div>
              
              {/* Active Page Content */}
              <div className="p-6 bg-gradient-to-b from-background/40 to-card/20">
                {activePageIndex >= 0 && activePageIndex < parsedContent.pages.length && (
                  <motion.div
                    key={activePageIndex}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.3 }}
                    className="space-y-6"
                  >
                    {/* Page Header */}
                    <div className="flex items-center justify-between pb-4 border-b border-border/20">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-r from-primary to-secondary text-white flex items-center justify-center font-medium text-sm shadow-lg">
                          {parsedContent.pages[activePageIndex].pageNumber || (activePageIndex + 1)}
                        </div>
                        {parsedContent.pages[activePageIndex].subtitle && (
                          <h3 className="text-lg font-semibold">
                            {parsedContent.pages[activePageIndex].subtitle}
                          </h3>
                        )}
                      </div>
                      <div className="text-sm text-muted-foreground bg-muted/50 px-3 py-1 rounded-full">
                        Page {activePageIndex + 1} of {parsedContent.pages.length}
                      </div>
                    </div>
                    
                    {/* Page Content */}
                    <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                      {parsedContent.pages[activePageIndex].paragraph ? (
                        <p className="text-base leading-relaxed">{parsedContent.pages[activePageIndex].paragraph}</p>
                      ) : parsedContent.pages[activePageIndex].content ? (
                        <p className="text-base leading-relaxed">{parsedContent.pages[activePageIndex].content}</p>
                      ) : (
                        <p className="text-muted-foreground italic">No content for this page</p>
                      )}
                    </div>
                    
                    {/* Page Media */}
                    {parsedContent.pages[activePageIndex].media && (
                      <div className="bg-gradient-to-r from-accent/10 to-primary/10 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                        <div className="flex items-center gap-3">
                          <div className="w-12 h-12 rounded-xl bg-gradient-to-r from-accent/20 to-primary/20 flex items-center justify-center">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                              <rect width="18" height="18" x="3" y="3" rx="2" ry="2" />
                              <circle cx="9" cy="9" r="2" />
                              <path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" />
                            </svg>
                          </div>
                          <div>
                            <p className="font-medium">Media Attachment</p>
                            <p className="text-sm text-muted-foreground">{parsedContent.pages[activePageIndex].media}</p>
                          </div>
                        </div>
                      </div>
                    )}
                    
                    {/* Page Navigation Controls */}
                    <div className="flex justify-between items-center pt-4 border-t border-border/20">
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => setActivePageIndex(prev => Math.max(0, prev - 1))}
                        disabled={activePageIndex === 0}
                        className="rounded-xl"
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
                        className="rounded-xl"
                      >
                        Next Page
                      </Button>
                    </div>
                  </motion.div>
                )}
              </div>
            </div>
          ) : parsedContent.sections ? (
            // If we have sections array
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
              <div className="space-y-4">
                {parsedContent.sections.map((section: any, index: number) => (
                  <div key={index} className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                    <h4 className="font-semibold mb-2 flex items-center gap-2">
                      <span className="w-6 h-6 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center text-xs font-medium">
                        {index + 1}
                      </span>
                      Section {index + 1}
                    </h4>
                    <p className="text-sm leading-relaxed">{section.text || 'No content'}</p>
                    {section.media && (
                      <div className="mt-3 p-3 bg-muted/50 rounded-lg">
                        <p className="text-xs text-muted-foreground">Media: {section.media}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ) : parsedContent.text ? (
            // If we have a single text field
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
              <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                <h4 className="font-semibold mb-3 flex items-center gap-2">
                  <span className="w-6 h-6 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center text-xs font-medium">
                    📄
                  </span>
                  Content
                </h4>
                <p className="text-sm leading-relaxed">{parsedContent.text}</p>
                {parsedContent.media && (
                  <div className="mt-3 p-3 bg-muted/50 rounded-lg">
                    <p className="text-xs text-muted-foreground">Media: {parsedContent.media}</p>
                  </div>
                )}
              </div>
            </div>
          ) : Object.keys(parsedContent).length > 0 ? (
            // If we have any content but not in expected format
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
              <div className="space-y-4">
                {Object.entries(parsedContent).map(([key, value], index) => {
                  // Handle special case for 'pages' key that's not in the expected format
                  if (key === 'pages' && Array.isArray(value)) {
                    return (
                      <div key={index} className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                        <h4 className="font-semibold mb-3 flex items-center gap-2">
                          <span className="w-6 h-6 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center text-xs font-medium">
                            📚
                          </span>
                          Pages
                        </h4>
                        <div className="space-y-3">
                          {value.map((page: any, pageIndex: number) => (
                            <div key={pageIndex} className="bg-muted/30 rounded-lg p-3">
                              <div className="flex items-center gap-2 mb-2">
                                <div className="w-6 h-6 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center text-xs font-medium">
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
                    <div key={index} className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                      <h4 className="font-semibold mb-2">{key}</h4>
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
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-8 text-center shadow-lg">
              <div className="text-4xl mb-3">📝</div>
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
      <div className="space-y-8">
        {/* Header Section */}
        <div className="bg-gradient-to-r from-secondary/5 to-accent/5 backdrop-blur-sm rounded-2xl p-6 border border-border/20">
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-r from-secondary/20 to-accent/20 flex items-center justify-center">
                <span className="text-lg">📝</span>
              </div>
              <div>
                <h3 className="text-xl font-bold text-foreground">Quiz Information</h3>
                <p className="text-sm text-muted-foreground">Template details and questions</p>
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Title</label>
                <div className="p-3 bg-background/60 backdrop-blur-sm rounded-xl border border-border/30">
                  <p className="font-medium">{data.title || 'Untitled Quiz'}</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Instructions</label>
                <div className="p-3 bg-background/60 backdrop-blur-sm rounded-xl border border-border/30 min-h-[60px]">
                  <p className="text-sm">{data.instruction || 'No instructions provided'}</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Max Score</label>
                <div className="p-3 bg-background/60 backdrop-blur-sm rounded-xl border border-border/30">
                  <p className="font-medium text-lg">{data.maxScore || '0'} points</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Quiz Questions Section */}
        <div className="space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-gradient-to-r from-accent/20 to-primary/20 flex items-center justify-center">
              <span className="text-sm">❓</span>
            </div>
            <h3 className="text-lg font-semibold">Quiz Questions</h3>
          </div>
          
          {hasQuestions ? (
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 overflow-hidden shadow-lg">
              {/* Question Navigation */}
              <div className="bg-muted/40 backdrop-blur-sm p-3 border-b border-border/20">
                <div className="flex overflow-x-auto gap-2">
                  {questions.map((question: any, index: number) => (
                    <motion.button
                      key={index}
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      onClick={() => setActiveQuestionIndex(index)}
                      className={`px-3 py-2 min-w-[40px] text-sm font-medium rounded-xl transition-all duration-200 ${
                        activeQuestionIndex === index 
                          ? 'bg-gradient-to-r from-secondary to-accent text-white shadow-lg' 
                          : 'bg-background/60 hover:bg-background/80 text-muted-foreground'
                      }`}
                    >
                      {index + 1}
                    </motion.button>
                  ))}
                </div>
              </div>
              
              {/* Active Question Display */}
              <div className="p-6 bg-gradient-to-b from-background/40 to-card/20">
                {activeQuestionIndex >= 0 && activeQuestionIndex < questions.length && (
                  <motion.div
                    key={activeQuestionIndex}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.3 }}
                    className="space-y-6"
                  >
                    {/* Question Header */}
                    <div className="flex items-center justify-between pb-4 border-b border-border/20">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-r from-secondary to-accent text-white flex items-center justify-center font-medium text-sm shadow-lg">
                          {activeQuestionIndex + 1}
                        </div>
                        <h3 className="text-lg font-semibold">Question {activeQuestionIndex + 1}</h3>
                      </div>
                      <div className="text-sm text-muted-foreground bg-muted/50 px-3 py-1 rounded-full">
                        Question {activeQuestionIndex + 1} of {questions.length}
                      </div>
                    </div>
                    
                    {/* Question Text */}
                    <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                      <p className="font-medium text-base leading-relaxed">
                        {questions[activeQuestionIndex].question || questions[activeQuestionIndex].text || 'No question text'}
                      </p>
                    </div>
                    
                    {/* Question Options */}
                    {questions[activeQuestionIndex].options && (
                      <div className="space-y-4">
                        <h4 className="text-base font-semibold flex items-center gap-2">
                          <span className="w-6 h-6 rounded-full bg-gradient-to-r from-accent/20 to-primary/20 flex items-center justify-center text-xs font-medium">
                            ✓
                          </span>
                          Answer Options
                        </h4>
                        <div className="space-y-3">
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
                                <motion.div 
                                  key={optIndex}
                                  whileHover={{ scale: 1.02 }}
                                  className={`flex items-center p-4 rounded-xl border transition-all duration-200 ${
                                    isCorrect 
                                      ? 'border-success/50 bg-success/10 shadow-lg' 
                                      : 'border-border/30 bg-background/60 hover:bg-background/80'
                                  }`}
                                >
                                  <div className={`w-6 h-6 rounded-full border flex items-center justify-center mr-4 transition-all duration-200 ${
                                    isCorrect 
                                      ? 'bg-success border-success text-white shadow-lg' 
                                      : 'border-border/50 bg-background'
                                  }`}>
                                    {isCorrect && (
                                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                                        <polyline points="20 6 9 17 4 12" />
                                      </svg>
                                    )}
                                  </div>
                                  <span className={`text-sm ${isCorrect ? 'font-semibold text-success-foreground' : 'text-foreground'}`}>
                                    {optionText}
                                  </span>
                                </motion.div>
                              );
                            })
                          )}
                        </div>
                      </div>
                    )}
                    
                    {/* Navigation Controls */}
                    <div className="flex justify-between items-center pt-4 border-t border-border/20">
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => setActiveQuestionIndex(prev => Math.max(0, prev - 1))}
                        disabled={activeQuestionIndex === 0}
                        className="rounded-xl"
                      >
                        Previous Question
                      </Button>
                      <div className="text-sm text-muted-foreground">
                        Question {activeQuestionIndex + 1} of {questions.length}
                      </div>
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => setActiveQuestionIndex(prev => Math.min(questions.length - 1, prev + 1))}
                        disabled={activeQuestionIndex === questions.length - 1}
                        className="rounded-xl"
                      >
                        Next Question
                      </Button>
                    </div>
                  </motion.div>
                )}
              </div>
            </div>
          ) : Object.keys(parsedContent).length > 0 ? (
            // If we have any content but not in expected format
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
              <div className="space-y-4">
                {Object.entries(parsedContent).map(([key, value], index) => (
                  <div key={index} className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                    <h4 className="font-semibold mb-2">{key}</h4>
                    <p className="text-sm">
                      {typeof value === 'string' ? value : JSON.stringify(value, null, 2)}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-8 text-center shadow-lg">
              <div className="text-4xl mb-3">📝</div>
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
      <div className="space-y-8">
        {/* Header Section */}
        <div className="bg-gradient-to-r from-accent/5 to-primary/5 backdrop-blur-sm rounded-2xl p-6 border border-border/20">
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-r from-accent/20 to-primary/20 flex items-center justify-center">
                <span className="text-lg">💪</span>
              </div>
              <div>
                <h3 className="text-xl font-bold text-foreground">Exercise Information</h3>
                <p className="text-sm text-muted-foreground">Template details and goals</p>
              </div>
            </div>
            
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Exercise Name</label>
                <div className="p-3 bg-background/60 backdrop-blur-sm rounded-xl border border-border/30">
                  <p className="font-medium text-lg">{exerciseName || 'Untitled Exercise'}</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground">Exercise Type</label>
                <div className="p-3 bg-background/60 backdrop-blur-sm rounded-xl border border-border/30">
                  <p className="font-medium">{exerciseType}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Exercise Goals Section */}
        <div className="space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center">
              <span className="text-sm">🎯</span>
            </div>
            <h3 className="text-lg font-semibold">Exercise Goals</h3>
          </div>
          
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {/* Repetitions */}
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-12 h-12 rounded-xl bg-gradient-to-r from-blue-500/20 to-blue-600/20 flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-blue-600">
                    <path d="m17 2 4 4-4 4" />
                    <path d="M3 11v-1a4 4 0 0 1 4-4h14" />
                    <path d="m7 22-4-4 4-4" />
                    <path d="M21 13v1a4 4 0 0 1-4 4H3" />
                  </svg>
                </div>
                <div>
                  <h4 className="font-semibold text-sm">Goal Reps</h4>
                  <p className="text-xs text-muted-foreground">Target repetitions</p>
                </div>
              </div>
              <p className="text-3xl font-bold text-blue-600">{goalReps}</p>
            </div>
            
            {/* Accuracy */}
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-12 h-12 rounded-xl bg-gradient-to-r from-green-500/20 to-green-600/20 flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-green-600">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                    <polyline points="22 4 12 14.01 9 11.01" />
                  </svg>
                </div>
                <div>
                  <h4 className="font-semibold text-sm">Goal Accuracy</h4>
                  <p className="text-xs text-muted-foreground">Target precision</p>
                </div>
              </div>
              <p className="text-3xl font-bold text-green-600">{accuracyDisplay}</p>
            </div>
            
            {/* Time */}
            <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-12 h-12 rounded-xl bg-gradient-to-r from-yellow-500/20 to-yellow-600/20 flex items-center justify-center">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-yellow-600">
                    <path d="M10 2h4" />
                    <path d="M12 14v-4" />
                    <path d="M12 14v-4" />
                    <circle cx="12" cy="14" r="8" />
                  </svg>
                </div>
                <div>
                  <h4 className="font-semibold text-sm">Goal Time</h4>
                  <p className="text-xs text-muted-foreground">Target duration</p>
                </div>
              </div>
              <p className="text-3xl font-bold text-yellow-600">{timeDisplay}</p>
            </div>
          </div>
        </div>
        
        {/* Additional Information */}
        {(difficulty || parsedContent.description || parsedContent.instructions) && (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-gradient-to-r from-secondary/20 to-accent/20 flex items-center justify-center">
                <span className="text-sm">ℹ️</span>
              </div>
              <h3 className="text-lg font-semibold">Additional Information</h3>
            </div>
            
            <div className="space-y-4">
              {/* Exercise Difficulty */}
              {difficulty && (
                <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-xl bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary">
                        <path d="m12 15 3.5-3.5" />
                        <path d="M20.3 18c.4-1 .7-2.2.7-3.4C21 9.8 17 6 12 6s-9 3.8-9 8.6c0 1.2.3 2.4.7 3.4" />
                      </svg>
                    </div>
                    <div>
                      <h4 className="font-semibold">Difficulty Level</h4>
                      <p className="text-lg font-medium capitalize text-primary">{difficulty}</p>
                    </div>
                  </div>
                </div>
              )}
              
              {/* Exercise Description/Instructions */}
              {(parsedContent.description || parsedContent.instructions) && (
                <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
                  <div className="space-y-3">
                    <h4 className="font-semibold flex items-center gap-2">
                      <span className="w-6 h-6 rounded-full bg-gradient-to-r from-accent/20 to-primary/20 flex items-center justify-center text-xs font-medium">
                        📋
                      </span>
                      Instructions
                    </h4>
                    <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                      <p className="text-sm leading-relaxed">{parsedContent.description || parsedContent.instructions}</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
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
        <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto bg-card/98 backdrop-blur-xl border-border/30 rounded-3xl custom-scrollbar">
          <DialogHeader className="pb-6">
            <DialogTitle className="text-xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              Template Review: {reviewTemplateData?.title || 'Template'}
            </DialogTitle>
            <DialogDescription className="text-muted-foreground">
              Reviewing {selectedTemplateType?.toLowerCase()} template details
            </DialogDescription>
          </DialogHeader>
          
          <div className="mt-4">
            {formatTemplateData(reviewTemplateData, selectedTemplateType?.toLowerCase() || '')}
          </div>
        </DialogContent>
      </Dialog>

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
          <motion.div
            initial={{ opacity: 0, y: 25 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="grid grid-cols-1 md:grid-cols-2 gap-6"
          >
            <FormField
              control={form.control}
              name="title"
              render={({ field }) => (
                <FormItem className="md:col-span-2">
                  <FormLabel className="text-lg font-semibold">Task Title</FormLabel>
                  <FormControl>
                    <Input 
                      placeholder="e.g., Week 1 Assignment" 
                      className="h-14 rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base"
                      {...field} 
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem className="md:col-span-2">
                  <FormLabel className="text-lg font-semibold">Description</FormLabel>
                  <FormControl>
                    <Textarea 
                      placeholder="Provide clear instructions and details for students..." 
                      className="min-h-[140px] rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base resize-none"
                      {...field} 
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Due Date */}
            <FormField
              control={form.control}
              name="dueDate"
              render={({ field }) => (
                <FormItem className="flex flex-col">
                  <FormLabel className="text-lg font-semibold">Due Date</FormLabel>
                  <Popover>
                    <PopoverTrigger asChild>
                      <FormControl>
                        <Button
                          variant={"outline"}
                          className={cn(
                            "w-full pl-4 text-left font-normal h-14 rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base",
                            !field.value && "text-muted-foreground"
                          )}
                        >
                          {field.value ? (
                            format(field.value, "PPP")
                          ) : (
                            <span>Select due date</span>
                          )}
                          <CalendarIcon className="ml-auto h-5 w-5 opacity-50" />
                        </Button>
                      </FormControl>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0 bg-card/90 backdrop-blur-xl border-border/30 rounded-2xl" align="start">
                      <Calendar
                        date={field.value}
                        onDateChange={field.onChange}
                        min={minDate}
                      />
                    </PopoverContent>
                  </Popover>
                  <FormMessage />
                </FormItem>
              )}
            />

            {/* Due Time */}
            <FormField
              control={form.control}
              name="dueTime"
              render={({ field }) => {
                // Format time to 12-hour format with AM/PM
                const formatTime = (time: string) => {
                  if (!time) return null;
                  const [hours, minutes] = time.split(':').map(Number);
                  const period = hours >= 12 ? 'PM' : 'AM';
                  const displayHours = hours % 12 || 12;
                  return `${displayHours}:${minutes.toString().padStart(2, '0')} ${period}`;
                };

                return (
                  <FormItem className="flex flex-col">
                    <FormLabel className="text-lg font-semibold">Due Time</FormLabel>
                    <Popover>
                      <PopoverTrigger asChild>
                        <FormControl>
                          <Button
                            variant={"outline"}
                            className={cn(
                              "w-full pl-4 text-left font-normal h-14 rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base",
                              !field.value && "text-muted-foreground"
                            )}
                          >
                            {field.value ? (
                              formatTime(field.value)
                            ) : (
                              <span>Select due time</span>
                            )}
                            <Clock className="ml-auto h-5 w-5 opacity-50" />
                          </Button>
                        </FormControl>
                      </PopoverTrigger>
                      <PopoverContent className="w-auto p-0 bg-card/90 backdrop-blur-xl border-border/30 rounded-2xl" align="start">
                        <div className="p-4">
                          <Input
                            type="time"
                            value={field.value}
                            onChange={(e) => field.onChange(e.target.value)}
                            className="h-12 rounded-xl border-border/30 bg-background/60 backdrop-blur-sm text-base"
                          />
                        </div>
                      </PopoverContent>
                    </Popover>
                    <FormDescription className="text-sm text-muted-foreground">
                      Set the specific time for the deadline
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                );
              }}
            />

            <FormField
              control={form.control}
              name="points"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-lg font-semibold">Max Attempts</FormLabel>
                  <FormControl>
                    <Input 
                      type="number" 
                      min={0} 
                      max={100} 
                      placeholder="3" 
                      className="h-14 rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base"
                      {...field}
                      onChange={(e) => field.onChange(Number(e.target.value))}
                    />
                  </FormControl>
                  <FormDescription className="text-sm text-muted-foreground">
                    Maximum number of attempts allowed
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 25 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="space-y-6"
          >
            <FormField
              control={form.control}
              name="templateType"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="text-lg font-semibold">Content Type</FormLabel>
                  <Select 
                    onValueChange={(value) => {
                      field.onChange(value);
                      handleTemplateTypeChange(value);
                    }}
                    defaultValue={field.value}
                  >
                    <FormControl>
                      <SelectTrigger className="w-full bg-background/60 backdrop-blur-sm border-border/30 rounded-2xl h-14 text-base">
                        <SelectValue placeholder="Choose content type" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent className="bg-card/90 backdrop-blur-xl border-border/30 rounded-2xl">
                      <SelectItem value="LESSON">📚 Lesson</SelectItem>
                      <SelectItem value="QUIZ">📝 Quiz</SelectItem>
                      <SelectItem value="EXERCISE">💪 Exercise</SelectItem>
                    </SelectContent>
                  </Select>
                  <FormDescription className="text-sm text-muted-foreground">
                    Select the type of content to assign to students
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
                    <div className="flex justify-between items-center mb-3">
                      <FormLabel className="text-lg font-semibold">Template</FormLabel>
                      <div className="flex gap-2">
                        <Button 
                          variant="ghost" 
                          size="sm" 
                          className="h-9 px-3 rounded-xl hover:bg-primary/10 hover:text-primary transition-all duration-200"
                          onClick={() => {
                            if (onSwitchToTemplate) {
                              const formData = form.getValues();
                              onSwitchToTemplate({
                                ...formData,
                                templateType: selectedTemplateType
                              });
                            }
                          }}
                          disabled={!selectedTemplateType}
                        >
                          <Plus className="h-4 w-4 mr-2" />
                          New Template
                        </Button>
                        <Button 
                          type="button"
                          variant="ghost" 
                          size="sm" 
                          className="h-9 px-3 rounded-xl hover:bg-secondary/10 hover:text-secondary transition-all duration-200"
                          onClick={async () => {
                            if (selectedTemplateId && selectedTemplateType) {
                              const templateType = selectedTemplateType.toLowerCase() as 'lesson' | 'quiz' | 'exercise';
                              try {
                                let data;
                                
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
                          <Eye className="h-4 w-4 mr-2" />
                          Preview
                        </Button>
                        <Button 
                          type="button"
                          variant="ghost" 
                          size="sm" 
                          className="h-9 px-3 rounded-xl hover:bg-destructive/10 hover:text-destructive transition-all duration-200"
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
                          <Trash className="h-4 w-4 mr-2" />
                          Delete
                        </Button>
                      </div>
                    </div>
                    <Select 
                      onValueChange={(value) => {
                        if (value === 'create-new') {
                          return;
                        }
                        field.onChange(value);
                        setSelectedTemplateId(value);
                      }}
                      value={field.value}
                      disabled={isLoadingTemplates()}
                    >
                      <FormControl>
                        <SelectTrigger className="w-full bg-background/60 backdrop-blur-sm border-border/30 rounded-2xl h-14 text-base">
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
                      <SelectContent className="bg-card/90 backdrop-blur-xl border-border/30 rounded-2xl">
                        {getAvailableTemplates().length === 0 ? (
                          <div className="p-4 text-center text-muted-foreground">
                            <div className="text-4xl mb-2">📝</div>
                            <p className="text-sm">No templates available</p>
                            <p className="text-xs">Create a template first</p>
                          </div>
                        ) : (
                          <>
                            <SelectGroup>
                              <SelectLabel className="text-sm font-medium text-muted-foreground">Available Templates</SelectLabel>
                              {getAvailableTemplates().map((template: any) => (
                                <SelectItem key={template.physicalId} value={template.physicalId} className="text-base">
                                  {template.title}
                                </SelectItem>
                              ))}
                            </SelectGroup>
                          </>
                        )}
                        <SelectSeparator />
                      </SelectContent>
                    </Select>
                    <FormDescription className="text-sm text-muted-foreground mt-2">
                      {selectedTemplateType === 'LESSON' && '📚 Select a lesson to assign to students'}
                      {selectedTemplateType === 'QUIZ' && '📝 Select a quiz to assign to students'}
                      {selectedTemplateType === 'EXERCISE' && '💪 Select an exercise to assign to students'}
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 25 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="flex justify-end gap-4 pt-8 border-t border-border/20"
          >
            <Button 
              type="button" 
              variant="outline" 
              onClick={onCancel}
              className="px-8 h-12 bg-card/80 backdrop-blur-xl border-border/30 hover:bg-card/90 rounded-2xl text-base font-medium transition-all duration-200"
            >
              Cancel
            </Button>
            <Button 
              type="submit" 
              disabled={createTaskMutation.isPending}
              className="px-8 h-12 bg-gradient-to-r from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg hover:shadow-xl transition-all duration-200 rounded-2xl text-base font-medium"
            >
              {createTaskMutation.isPending ? (
                <>
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  Creating Task...
                </>
              ) : (
                <>
                  <Plus className="mr-2 h-5 w-5" />
                  Create Task
                </>
              )}
            </Button>
          </motion.div>
        </form>
      </Form>

    {/* Delete Template Confirmation Dialog */}
    <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
      <AlertDialogContent className="bg-card/98 backdrop-blur-xl border-border/30 rounded-3xl">
        <AlertDialogHeader>
          <AlertDialogTitle className="text-xl font-bold">Confirm Deletion</AlertDialogTitle>
          <AlertDialogDescription className="text-base">
            This action cannot be undone. This will permanently delete the {templateToDelete?.type?.toLowerCase()} template.
            <br />
            <br />
            <strong>Note:</strong> You can only delete templates that you created yourself.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter className="gap-3">
          <AlertDialogCancel className="rounded-xl">Cancel</AlertDialogCancel>
          <AlertDialogAction 
            onClick={() => {
              if (templateToDelete) {
                deleteTemplateMutation.mutate({ 
                  templateId: templateToDelete.id, 
                  templateType: templateToDelete.type 
                });
                setDeleteDialogOpen(false);
              }
            }}
            className="bg-red-500 hover:bg-red-600 rounded-xl"
          >
            {deleteTemplateMutation.isPending ? (
              <span className="flex items-center">
                <span className="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-r-transparent"></span>
                Deleting...
              </span>
            ) : 'Delete Template'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </>
  );
}
