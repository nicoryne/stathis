'use client';

import { useState, useEffect, useMemo } from 'react';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ClipboardList, Search, Filter, Grid3X3, List, X, MoreHorizontal, Edit, Trash2, ArrowLeft, Plus, CalendarIcon, Eye, Trash, Loader2 } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { getClassroomTasks, startTask, deactivateTask, deleteTask, updateTask } from '@/services/tasks/api-task-client';
import { getTeacherLessonTemplates, getTeacherQuizTemplates, getTeacherExerciseTemplates } from '@/services/templates/api-template-client';
import { TaskResponseDTO } from '@/services/tasks/api-task-client';
import { CreateTaskForm } from './create-task-form';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { format } from 'date-fns';
import { toast } from 'sonner';
import { DeleteTaskModal } from '@/components/ui/delete-task-modal';
import { EditTaskModal } from '@/components/ui/edit-task-modal';
import { CreateTaskModal } from '@/components/ui/create-task-modal';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  SelectGroup,
  SelectLabel,
} from "@/components/ui/select";
import { TemplateCreationModal } from '../templates/template-creation-modal';
import { CreateLessonForm } from '../templates/create-lesson-form';
import { CreateQuizForm } from '../templates/create-quiz-form';
import { CreateExerciseForm } from '../templates/create-exercise-form';
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface TaskCreationTabProps {
  classroomId: string;
}

// Inline Template Creation Component
interface InlineTemplateCreationProps {
  templateType: 'LESSON' | 'QUIZ' | 'EXERCISE';
  onTemplateCreated: () => void;
  onCancel: () => void;
}

const InlineTemplateCreation = ({ templateType, onTemplateCreated, onCancel }: InlineTemplateCreationProps) => {
  const [activeTab, setActiveTab] = useState<'lesson' | 'quiz' | 'exercise'>(
    templateType === 'LESSON' ? 'lesson' : 
    templateType === 'QUIZ' ? 'quiz' : 
    templateType === 'EXERCISE' ? 'exercise' : 
    'lesson'
  );

  const handleSuccess = () => {
    onTemplateCreated();
  };

  return (
    <div className="space-y-8">
      <motion.div
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="bg-card/60 backdrop-blur-sm rounded-3xl border border-border/30 p-8 shadow-lg"
      >
        <div className="flex items-center gap-4 mb-8">
          <div className="relative">
            <div className="absolute -inset-2 rounded-full bg-gradient-to-r from-secondary/20 to-accent/20 blur-md" />
            <div className="relative w-10 h-10 rounded-full bg-gradient-to-r from-secondary to-accent flex items-center justify-center">
              <Plus className="h-5 w-5 text-white" />
            </div>
          </div>
          <div>
            <h3 className="text-xl font-bold bg-gradient-to-r from-secondary to-accent bg-clip-text text-transparent">
              Template Creation
            </h3>
            <p className="text-muted-foreground mt-1">
              Choose the type of template you want to create
            </p>
          </div>
        </div>
        
        <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as 'lesson' | 'quiz' | 'exercise')} className="w-full">
          <TabsList className="grid grid-cols-3 w-full bg-muted/40 backdrop-blur-sm border border-border/30 rounded-2xl p-2 h-14">
            <TabsTrigger 
              value="lesson" 
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-primary data-[state=active]:to-secondary data-[state=active]:text-white rounded-xl transition-all duration-300 text-sm font-medium h-10"
            >
              Lesson
            </TabsTrigger>
            <TabsTrigger 
              value="quiz"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-secondary data-[state=active]:to-accent data-[state=active]:text-white rounded-xl transition-all duration-300 text-sm font-medium h-10"
            >
              Quiz
            </TabsTrigger>
            <TabsTrigger 
              value="exercise"
              className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-accent data-[state=active]:to-primary data-[state=active]:text-white rounded-xl transition-all duration-300 text-sm font-medium h-10"
            >
              Exercise
            </TabsTrigger>
          </TabsList>
          
          <TabsContent value="lesson" className="mt-8">
            <motion.div
              initial={{ opacity: 0, x: -25 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.4 }}
            >
              <CreateLessonForm 
                onSuccess={handleSuccess}
                onCancel={onCancel}
              />
            </motion.div>
          </TabsContent>
          
          <TabsContent value="quiz" className="mt-8">
            <motion.div
              initial={{ opacity: 0, x: -25 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.4 }}
            >
              <CreateQuizForm 
                onSuccess={handleSuccess}
                onCancel={onCancel}
              />
            </motion.div>
          </TabsContent>
          
          <TabsContent value="exercise" className="mt-8">
            <motion.div
              initial={{ opacity: 0, x: -25 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.4 }}
            >
              <CreateExerciseForm 
                onSuccess={handleSuccess}
                onCancel={onCancel}
              />
            </motion.div>
          </TabsContent>
        </Tabs>
      </motion.div>
    </div>
  );
};

export function TaskCreationTab({ classroomId }: TaskCreationTabProps) {
  const [creatingTask, setCreatingTask] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<TaskResponseDTO | null>(null);
  const [taskTypeFilter, setTaskTypeFilter] = useState<'ALL' | 'LESSON' | 'QUIZ' | 'EXERCISE'>('ALL');
  
  // New UX state variables
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [tasksPerPage] = useState(10);
  
  // Sequential modal flow state
  const [modalMode, setModalMode] = useState<'task' | 'template'>('task');
  const [taskFormData, setTaskFormData] = useState<any>(null);
  
  // Template type selection state
  const [selectedTemplateType, setSelectedTemplateType] = useState<string | null>(null);
  const [selectedTemplateId, setSelectedTemplateId] = useState<string>('');
  
  // Form state for editing a task
  const [editTaskName, setEditTaskName] = useState('');
  const [editTaskDescription, setEditTaskDescription] = useState('');
  const [editTaskSubmissionDate, setEditTaskSubmissionDate] = useState('');
  const [editTaskClosingDate, setEditTaskClosingDate] = useState('');
  const [editTaskMaxAttempts, setEditTaskMaxAttempts] = useState<number | undefined>(undefined);
  
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

  // Fetch tasks for this classroom
  const { 
    data: tasks, 
    isLoading: isLoadingTasks,
    error: tasksError,
    refetch: refetchTasks
  } = useQuery({
    queryKey: ['classroom-tasks', classroomId],
    queryFn: () => getClassroomTasks(classroomId),
    enabled: !!classroomId,
  });

  const handleCreateTask = () => {
    setCreatingTask(true);
    setModalMode('task');
    setTaskFormData(null);
  };

  const handleCancelCreation = () => {
    setCreatingTask(false);
    setModalMode('task');
    setTaskFormData(null);
  };

  const handleTaskCreated = () => {
    setCreatingTask(false);
    setModalMode('task');
    setTaskFormData(null);
    refetchTasks();
  };

  // Sequential modal flow handlers
  const handleSwitchToTemplate = (formData: any) => {
    setTaskFormData(formData);
    setModalMode('template');
  };

  const handleBackToTask = () => {
    setModalMode('task');
  };

  const handleTemplateCreated = () => {
    // Return to task creation after template is created
    setModalMode('task');
    refetchTasks();
  };
  
  // Mutation for starting a task
  const startTaskMutation = useMutation({
    mutationFn: (physicalId: string) => startTask(physicalId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classroom-tasks', classroomId] });
      toast.success('Task started successfully');
    },
    onError: (error) => {
      console.error('Error starting task:', error);
      toast.error('Failed to start task: ' + (error as Error).message);
    }
  });
  
  // Mutation for deactivating a task
  const deactivateTaskMutation = useMutation({
    mutationFn: (physicalId: string) => deactivateTask(physicalId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classroom-tasks', classroomId] });
      toast.success('Task deactivated successfully');
    },
    onError: (error) => {
      console.error('Error deactivating task:', error);
      toast.error('Failed to deactivate task: ' + (error as Error).message);
    }
  });
  
  // Handle task start button click
  const handleStartTask = (physicalId: string) => {
    startTaskMutation.mutate(physicalId);
  };
  
  // Handle task deactivate button click
  const handleDeactivateTask = (physicalId: string) => {
    deactivateTaskMutation.mutate(physicalId);
  };
  
  // Mutation for deleting a task
  const deleteTaskMutation = useMutation({
    mutationFn: (physicalId: string) => deleteTask(physicalId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classroom-tasks', classroomId] });
      toast.success('Task deleted successfully');
      setDeleteDialogOpen(false);
      setSelectedTask(null);
    },
    onError: (error) => {
      console.error('Error deleting task:', error);
      toast.error('Failed to delete task: ' + (error as Error).message);
      setDeleteDialogOpen(false);
      setSelectedTask(null);
    }
  });
  
  // Handle task delete confirmation
  const handleDeleteTask = () => {
    if (selectedTask) {
      deleteTaskMutation.mutate(selectedTask.physicalId);
    }
  };
  
  // Open delete confirmation dialog
  const openDeleteDialog = (task: TaskResponseDTO) => {
    setSelectedTask(task);
    setDeleteDialogOpen(true);
  };
  
  // Open edit task dialog
  const openEditDialog = (task: TaskResponseDTO) => {
    setSelectedTask(task);
    // Populate form with task data
    setEditTaskName(task.name);
    setEditTaskDescription(task.description || '');
    setEditTaskSubmissionDate(task.submissionDate);
    setEditTaskClosingDate(task.closingDate);
    setEditTaskMaxAttempts(task.maxAttempts);
    
    // Determine template type from task data
    if (task.exerciseTemplateId) {
      setSelectedTemplateType('EXERCISE');
      setSelectedTemplateId(task.exerciseTemplateId);
    } else if (task.lessonTemplateId) {
      setSelectedTemplateType('LESSON');
      setSelectedTemplateId(task.lessonTemplateId);
    } else if (task.quizTemplateId) {
      setSelectedTemplateType('QUIZ');
      setSelectedTemplateId(task.quizTemplateId);
    } else {
      setSelectedTemplateType(null);
      setSelectedTemplateId('');
    }
    
    setEditDialogOpen(true);
  };
  
  // Helper function to handle template type change
  const handleTemplateTypeChange = (value: string) => {
    setSelectedTemplateType(value);
    setSelectedTemplateId('');
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
  
  
  // Mutation for updating a task
  const updateTaskMutation = useMutation({
    mutationFn: ({ physicalId, taskData }: { physicalId: string, taskData: Partial<TaskResponseDTO> }) => 
      updateTask(physicalId, taskData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['classroom-tasks', classroomId] });
      toast.success('Task updated successfully');
      setEditDialogOpen(false);
      setSelectedTask(null);
    },
    onError: (error) => {
      console.error('Error updating task:', error);
      toast.error('Failed to update task: ' + (error as Error).message);
      setEditDialogOpen(false);
      setSelectedTask(null);
    }
  });
  
  // Handle save task updates
  const handleUpdateTask = () => {
    if (!selectedTask) return;
    
    // Create update data object with all required fields from TaskBodyDTO
    const updateData: Partial<TaskResponseDTO> = {
      // Required fields
      name: editTaskName,
      description: editTaskDescription,
      submissionDate: editTaskSubmissionDate,
      closingDate: editTaskClosingDate,
      classroomPhysicalId: selectedTask.classroomPhysicalId,
      
      // Optional fields
      maxAttempts: editTaskMaxAttempts,
      imageUrl: selectedTask.imageUrl
    };
    
    // Set the appropriate template ID based on selected type and clear others
    // This helps avoid sending multiple template IDs which would be invalid
    if (selectedTemplateType === 'EXERCISE' && selectedTemplateId) {
      // Pattern must be: ^EXERCISE-[A-Z0-9-]+$
      let exerciseId = selectedTemplateId;
      if (!exerciseId.startsWith('EXERCISE-')) {
        exerciseId = exerciseId.includes('EXERCISE-') ? exerciseId : `EXERCISE-${exerciseId}`;
      }
      updateData.exerciseTemplateId = exerciseId.toUpperCase();
      updateData.lessonTemplateId = undefined;
      updateData.quizTemplateId = undefined;
    } else if (selectedTemplateType === 'LESSON' && selectedTemplateId) {
      // Pattern must be: ^LESSON-[A-Z0-9-]+$
      let lessonId = selectedTemplateId;
      if (!lessonId.startsWith('LESSON-')) {
        lessonId = lessonId.includes('LESSON-') ? lessonId : `LESSON-${lessonId}`;
      }
      updateData.lessonTemplateId = lessonId.toUpperCase();
      updateData.exerciseTemplateId = undefined;
      updateData.quizTemplateId = undefined;
    } else if (selectedTemplateType === 'QUIZ' && selectedTemplateId) {
      // Pattern must be: ^[A-Za-z0-9-]+$
      updateData.quizTemplateId = selectedTemplateId;
      updateData.exerciseTemplateId = undefined;
      updateData.lessonTemplateId = undefined;
    }
    
    console.log('Sending task update:', updateData);
    
    // Call update mutation
    updateTaskMutation.mutate({
      physicalId: selectedTask.physicalId,
      taskData: updateData
    });
  };

  // Helper function to format date
  const formatDate = (dateString: string) => {
    if (!dateString) return 'No date set';
    
    try {
      // Parse ISO date string, handle with or without milliseconds
      const date = new Date(dateString);
      
      // Check if date is valid
      if (isNaN(date.getTime())) {
        console.error('Invalid date string:', dateString);
        return 'Invalid date format';
      }
      
      return format(date, 'PPP');
    } catch (e) {
      console.error('Error formatting date:', e);
      return 'Date format error';
    }
  };

  // Helper function to get template type based on which template ID is present
  const getTemplateType = (task: TaskResponseDTO) => {
    if (task.exerciseTemplateId) return 'EXERCISE';
    if (task.lessonTemplateId) return 'LESSON';
    if (task.quizTemplateId) return 'QUIZ';
    return 'UNKNOWN';
  };
  
  // Helper function to get status badge variant
  const getStatusBadge = (task: TaskResponseDTO) => {
    if (!task) return <Badge variant="outline" className="text-muted-foreground">Unknown</Badge>;
    
    // Determine status based on active and started flags
    if (task.active && task.started) {
      return <Badge variant="default" className="bg-green-100 text-green-700 dark:bg-green-950/40 dark:text-green-400">Active</Badge>;
    } else if (task.active && !task.started) {
      return <Badge variant="secondary" className="bg-blue-100 text-blue-700 dark:bg-blue-950/40 dark:text-blue-400">Not Started</Badge>;
    } else {
      return <Badge variant="outline" className="text-muted-foreground">Inactive</Badge>;
    }
  };

  // Helper function to get template type label
  const getTemplateTypeLabel = (type: string) => {
    switch (type) {
      case 'LESSON':
        return 'Lesson';
      case 'QUIZ':
        return 'Quiz';
      case 'EXERCISE':
        return 'Exercise';
      default:
        return 'Unknown';
    }
  };

  // Helper function to get task type color
  const getTaskTypeColor = (type: string) => {
    switch (type) {
      case 'LESSON':
        return 'bg-blue-100 text-blue-700 dark:bg-blue-950/40 dark:text-blue-400';
      case 'QUIZ':
        return 'bg-purple-100 text-purple-700 dark:bg-purple-950/40 dark:text-purple-400';
      case 'EXERCISE':
        return 'bg-green-100 text-green-700 dark:bg-green-950/40 dark:text-green-400';
      default:
        return 'bg-gray-100 text-gray-700 dark:bg-gray-950/40 dark:text-gray-400';
    }
  };

  // Filter, search, and sort tasks with pagination
  const filteredAndSortedTasks = useMemo(() => {
    if (!tasks) return [];
    
    // Filter by type
    let filtered = tasks;
    if (taskTypeFilter !== 'ALL') {
      filtered = tasks.filter(task => getTemplateType(task) === taskTypeFilter);
    }
    
    // Search filter
    if (searchTerm.trim()) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(task => 
        task.name.toLowerCase().includes(searchLower) ||
        (task.description && task.description.toLowerCase().includes(searchLower))
      );
    }
    
    // Sort by submission date (earliest first)
    return filtered.sort((a, b) => {
      const dateA = new Date(a.submissionDate).getTime();
      const dateB = new Date(b.submissionDate).getTime();
      return dateA - dateB;
    });
  }, [tasks, taskTypeFilter, searchTerm]);

  // Pagination logic
  const totalPages = Math.ceil(filteredAndSortedTasks.length / tasksPerPage);
  const paginatedTasks = useMemo(() => {
    const startIndex = (currentPage - 1) * tasksPerPage;
    return filteredAndSortedTasks.slice(startIndex, startIndex + tasksPerPage);
  }, [filteredAndSortedTasks, currentPage, tasksPerPage]);

  // Reset to first page when filters change
  useEffect(() => {
    setCurrentPage(1);
  }, [taskTypeFilter, searchTerm]);

  // Helper function to check if a task is overdue
  const isTaskOverdue = (task: TaskResponseDTO): boolean => {
    if (!task.closingDate) return false;
    
    try {
      const closingDate = new Date(task.closingDate);
      const now = new Date();
      return closingDate < now;
    } catch (e) {
      console.error('Error parsing closing date:', e);
      return false;
    }
  };

  // Auto-deactivate overdue tasks
  useEffect(() => {
    if (!tasks || tasks.length === 0) return;

    // Check for overdue tasks that are still active
    const overdueTasks = tasks.filter(task => 
      task.active && isTaskOverdue(task)
    );

    // Deactivate each overdue task
    overdueTasks.forEach(task => {
      console.log(`Auto-deactivating overdue task: ${task.name} (closing date: ${task.closingDate})`);
      deactivateTaskMutation.mutate(task.physicalId);
    });
  }, [tasks]); // Run when tasks change

  // Set up periodic check for overdue tasks (every minute)
  useEffect(() => {
    const intervalId = setInterval(() => {
      if (!tasks || tasks.length === 0) return;

      const overdueTasks = tasks.filter(task => 
        task.active && isTaskOverdue(task)
      );

      overdueTasks.forEach(task => {
        console.log(`Auto-deactivating overdue task (periodic check): ${task.name}`);
        deactivateTaskMutation.mutate(task.physicalId);
      });
    }, 60000); // Check every 60 seconds

    return () => clearInterval(intervalId);
  }, [tasks]);

  return (
    <div className="space-y-6">
      {/* Delete Confirmation Modal */}
      <DeleteTaskModal
        open={deleteDialogOpen}
        onClose={() => {
          setDeleteDialogOpen(false);
          setSelectedTask(null);
        }}
        title={
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-full bg-destructive/10">
              <Trash2 className="h-5 w-5 text-destructive" />
            </div>
            <span className="text-destructive">Delete Task</span>
          </div>
        }
        description={`This action cannot be undone. This will permanently delete the task "${selectedTask?.name}" and remove it from the classroom.`}
        onConfirm={handleDeleteTask}
        isDeleting={deleteTaskMutation.isPending}
      />
      
      {/* Edit Task Modal */}
      <EditTaskModal
        open={editDialogOpen}
        onClose={() => {
          setEditDialogOpen(false);
          setSelectedTask(null);
        }}
        title={
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-full bg-primary/10">
              <Edit className="h-5 w-5 text-primary" />
            </div>
            <span className="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">Edit Task</span>
          </div>
        }
        description="Update task details and settings"
      >
        <div className="space-y-6">
            <motion.div
              initial={{ opacity: 0, y: 25 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
              className="grid grid-cols-1 md:grid-cols-2 gap-6"
            >
              <div className="md:col-span-2">
                <label htmlFor="taskName" className="text-lg font-semibold block mb-3">Task Name</label>
                <Input
                  id="taskName"
                  value={editTaskName}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditTaskName(e.target.value)}
                  placeholder="e.g., Week 1 Assignment"
                  className="h-14 rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base"
                />
              </div>
              
              <div className="md:col-span-2">
                <label htmlFor="taskDescription" className="text-lg font-semibold block mb-3">Description</label>
                <Textarea
                  id="taskDescription"
                  value={editTaskDescription}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setEditTaskDescription(e.target.value)}
                  placeholder="Provide clear instructions and details for students..."
                  className="min-h-[140px] rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base resize-none"
                />
              </div>
            
              <div className="md:col-span-2">
                <label className="text-lg font-semibold block mb-3">Due Date</label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button
                      variant="outline"
                      className="w-full h-14 pl-4 text-left font-normal rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base justify-start"
                    >
                      {editTaskSubmissionDate ? (
                        format(new Date(editTaskSubmissionDate), "PPP")
                      ) : (
                        <span className="text-muted-foreground">Select due date</span>
                      )}
                      <CalendarIcon className="ml-auto h-5 w-5 opacity-50" />
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0 bg-card/90 backdrop-blur-xl border-border/30 rounded-2xl" align="start">
                    <Calendar
                      date={editTaskSubmissionDate ? new Date(editTaskSubmissionDate) : undefined}
                      onDateChange={(date) => {
                        if (date) {
                          setEditTaskSubmissionDate(date.toISOString());
                          setEditTaskClosingDate(date.toISOString());
                        }
                      }}
                      min={minDate}
                    />
                  </PopoverContent>
                </Popover>
              </div>
            
              <div>
                <label htmlFor="taskMaxAttempts" className="text-lg font-semibold block mb-3">Max Attempts</label>
                <Input
                  id="taskMaxAttempts"
                  type="number"
                  min={0}
                  max={100}
                  placeholder="3"
                  value={editTaskMaxAttempts?.toString() || ''}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditTaskMaxAttempts(e.target.value ? parseInt(e.target.value) : undefined)}
                  className="h-14 rounded-2xl border-border/30 bg-background/60 backdrop-blur-sm text-base"
                />
                <p className="text-sm text-muted-foreground mt-2">Maximum number of attempts allowed</p>
              </div>
            
              <div>
                <label htmlFor="templateType" className="text-lg font-semibold block mb-3">Content Type</label>
                <Select 
                  onValueChange={handleTemplateTypeChange}
                  value={selectedTemplateType || undefined}
                >
                  <SelectTrigger className="w-full bg-background/60 backdrop-blur-sm border-border/30 rounded-2xl h-14 text-base">
                    <SelectValue placeholder="Choose content type" />
                  </SelectTrigger>
                  <SelectContent className="bg-card/90 backdrop-blur-xl border-border/30 rounded-2xl">
                    <SelectItem value="LESSON">📚 Lesson</SelectItem>
                    <SelectItem value="QUIZ">📝 Quiz</SelectItem>
                    <SelectItem value="EXERCISE">💪 Exercise</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground mt-2">Select the type of content to assign to students</p>
              </div>
            
            {/* Template Selection */}
            {selectedTemplateType && (
              <div className="md:col-span-2">
                <div className="flex justify-between items-center mb-3">
                  <label htmlFor="templateId" className="text-lg font-semibold">Template</label>
                  <TemplateCreationModal 
                    templateType={selectedTemplateType as 'LESSON' | 'QUIZ' | 'EXERCISE'} 
                    onTemplateCreated={handleTemplateCreated}
                    continueToTask={true}
                    trigger={
                      <Button 
                        variant="ghost" 
                        size="sm" 
                        className="h-9 px-3 rounded-xl hover:bg-primary/10 hover:text-primary transition-all duration-200"
                      >
                        <Plus className="h-4 w-4 mr-2" />
                        New Template
                      </Button>
                    }
                  />
                </div>
                <Select 
                  onValueChange={(value) => setSelectedTemplateId(value)}
                  value={selectedTemplateId}
                  disabled={isLoadingTemplates()}
                >
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
                  </SelectContent>
                </Select>
                <p className="text-sm text-muted-foreground mt-2">
                  {selectedTemplateType === 'LESSON' && '📚 Select a lesson to assign to students'}
                  {selectedTemplateType === 'QUIZ' && '📝 Select a quiz to assign to students'}
                  {selectedTemplateType === 'EXERCISE' && '💪 Select an exercise to assign to students'}
                </p>
              </div>
            )}
            </motion.div>
          
          <div className="flex justify-end gap-4 pt-8 border-t border-border/20">
            <Button 
              variant="outline" 
              onClick={() => {
                setEditDialogOpen(false);
                setSelectedTask(null);
              }}
              className="px-8 h-12 bg-card/80 backdrop-blur-xl border-border/30 hover:bg-card/90 rounded-2xl text-base font-medium transition-all duration-200"
            >
              Cancel
            </Button>
            <Button 
              onClick={handleUpdateTask}
              disabled={updateTaskMutation.isPending}
              className="px-8 h-12 bg-gradient-to-r from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg hover:shadow-xl transition-all duration-200 rounded-2xl text-base font-medium"
            >
              {updateTaskMutation.isPending ? (
                <>
                  <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                  Saving Changes...
                </>
              ) : (
                <>
                  <Edit className="mr-2 h-5 w-5" />
                  Save Changes
                </>
              )}
            </Button>
          </div>
        </div>
      </EditTaskModal>
      
      {/* Enhanced Header with Search and Controls */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight">Tasks</h2>
            <p className="text-sm text-muted-foreground mt-1">
              {filteredAndSortedTasks.length} of {tasks?.length || 0} tasks
              {searchTerm && ` matching "${searchTerm}"`}
            </p>
          </div>
          {!creatingTask && (
            <motion.div
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              transition={{ duration: 0.2 }}
            >
              <Button
                onClick={handleCreateTask}
                className="h-9 bg-gradient-to-r from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 text-white shadow-lg hover:shadow-xl transition-all duration-200 rounded-xl"
              >
                <div className="relative">
                  <div className="absolute -inset-1 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 blur-sm" />
                  <Plus className="relative mr-2 h-4 w-4" />
                </div>
                Create Task
              </Button>
            </motion.div>
          )}
        </div>

        {/* Enhanced Search and Filter Controls */}
        {!creatingTask && tasks && tasks.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
            className="flex flex-col sm:flex-row gap-3"
          >
            {/* Enhanced Search Bar */}
            <div className="relative flex-1">
              <div className="absolute left-3 top-1/2 transform -translate-y-1/2">
                <div className="relative">
                  <div className="absolute -inset-1 rounded-full bg-gradient-to-r from-primary/10 to-secondary/10 blur-sm" />
                  <Search className="relative h-4 w-4 text-muted-foreground" />
                </div>
              </div>
              <Input
                placeholder="Search tasks by name or description..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 h-9 bg-card/90 backdrop-blur-xl border-border/50 rounded-xl focus:ring-2 focus:ring-primary/20 transition-all duration-200"
              />
              {searchTerm && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.8 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.2 }}
                >
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setSearchTerm('')}
                    className="absolute right-2 top-1/2 transform -translate-y-1/2 h-6 w-6 p-0 hover:bg-destructive/10 hover:text-destructive rounded-full"
                  >
                    <X className="h-3 w-3" />
                  </Button>
                </motion.div>
              )}
            </div>

            {/* Enhanced Filter Chips */}
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-sm text-muted-foreground whitespace-nowrap font-medium">Filter:</span>
              <div className="flex gap-1 flex-wrap">
                {(['ALL', 'LESSON', 'QUIZ', 'EXERCISE'] as const).map((filter) => (
                  <motion.div
                    key={filter}
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                    transition={{ duration: 0.2 }}
                  >
                    <Button
                      variant={taskTypeFilter === filter ? 'default' : 'outline'}
                      size="sm"
                      onClick={() => setTaskTypeFilter(filter)}
                      className={`h-8 px-3 text-xs rounded-lg transition-all duration-200 ${
                        taskTypeFilter === filter 
                          ? 'bg-gradient-to-r from-primary to-secondary text-white shadow-lg' 
                          : 'bg-card/90 backdrop-blur-xl border-border/50 hover:bg-card/95 hover:border-primary/30'
                      }`}
                    >
                      {filter === 'ALL' ? 'All' : 
                       filter === 'LESSON' ? 'Lessons' :
                       filter === 'QUIZ' ? 'Quizzes' : 'Exercises'}
                    </Button>
                  </motion.div>
                ))}
              </div>
            </div>

          </motion.div>
        )}
      </div>

      {/* Enhanced Sequential Modal Flow */}
      <CreateTaskModal
        open={creatingTask}
        onClose={() => {
          setCreatingTask(false);
          setModalMode('task');
          setTaskFormData(null);
        }}
        title={
          modalMode === 'task' ? (
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-primary/10">
                <Plus className="h-5 w-5 text-primary" />
              </div>
              <span className="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">Create New Task</span>
            </div>
          ) : (
            <div className="flex items-center gap-3">
              <Button
                variant="ghost"
                size="sm"
                onClick={handleBackToTask}
                className="h-8 w-8 p-0 hover:bg-muted/60 rounded-full"
              >
                <ArrowLeft className="h-4 w-4" />
              </Button>
              <div className="p-2 rounded-full bg-secondary/10">
                <Plus className="h-5 w-5 text-secondary" />
              </div>
              <span className="bg-gradient-to-r from-secondary to-accent bg-clip-text text-transparent">Create New Template</span>
            </div>
          )
        }
        description={modalMode === 'task' ? "Set up a new assignment for your students" : "Build a reusable template for your tasks"}
      >
        {modalMode === 'task' ? (
          <motion.div
            initial={{ opacity: 0, y: 25 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1, ease: "easeOut" }}
          >
            <CreateTaskForm 
              classroomPhysicalId={classroomId} 
              onSuccess={handleTaskCreated} 
              onCancel={handleCancelCreation}
              onSwitchToTemplate={handleSwitchToTemplate}
            />
          </motion.div>
        ) : (
          <motion.div
            initial={{ opacity: 0, y: 25 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1, ease: "easeOut" }}
          >
            <InlineTemplateCreation 
              templateType={taskFormData?.templateType as 'LESSON' | 'QUIZ' | 'EXERCISE'} 
              onTemplateCreated={handleTemplateCreated}
              onCancel={handleBackToTask}
            />
          </motion.div>
        )}
      </CreateTaskModal>

      {!creatingTask && (
        <div className="space-y-4">
          {isLoadingTasks ? (
            <div className="flex justify-center py-8">
              <div className="animate-pulse flex space-x-4">
                <div className="flex-1 space-y-4 py-1">
                  <div className="h-4 bg-muted rounded w-3/4"></div>
                  <div className="space-y-2">
                    <div className="h-4 bg-muted rounded"></div>
                    <div className="h-4 bg-muted rounded w-5/6"></div>
                  </div>
                </div>
              </div>
            </div>
          ) : tasksError || !tasks || tasks.length === 0 ? (
            <Card className="border-dashed">
              <CardHeader>
                <CardTitle className="text-center text-muted-foreground">No Tasks Created</CardTitle>
              </CardHeader>
              <CardContent className="flex flex-col items-center justify-center py-8">
                <ClipboardList className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-center text-muted-foreground max-w-sm mb-4">
                  You haven't created any tasks for this classroom yet. Tasks allow you to assign lessons, quizzes, and exercises to students.
                </p>
                <Button onClick={handleCreateTask}>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Your First Task
                </Button>
              </CardContent>
            </Card>
          ) : filteredAndSortedTasks.length === 0 ? (
            <Card className="border-dashed">
              <CardContent className="flex flex-col items-center justify-center py-12">
                <ClipboardList className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-center text-muted-foreground">
                  No {taskTypeFilter.toLowerCase()} tasks found
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {/* Task List - Compact Grid Only */}
              <div className="grid gap-3 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
                {paginatedTasks.map((task) => {
                  const taskType = getTemplateType(task);
                  return (
                    <Card key={task.physicalId} className="bg-card/90 backdrop-blur-xl border-border/50 hover:shadow-lg transition-all duration-200">
                      <CardHeader className="pb-2">
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 mb-1">
                              <Badge variant="secondary" className={`text-xs px-2 py-0.5 ${getTaskTypeColor(taskType)}`}>
                                {getTemplateTypeLabel(taskType)}
                              </Badge>
                              <h3 className="text-sm font-medium truncate">{task.name}</h3>
                            </div>
                            <p className="text-xs text-muted-foreground">
                              Due {formatDate(task.submissionDate)}
                            </p>
                          </div>
                          <div className="flex items-center gap-1">
                            <div className="flex-shrink-0">
                              {getStatusBadge(task)}
                            </div>
                            {/* Kebab Menu */}
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="h-6 w-6 p-0 hover:bg-muted/60 hover:text-muted-foreground rounded-md transition-all duration-150"
                                >
                                  <MoreHorizontal className="h-3 w-3" />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent 
                                align="end" 
                                className="w-44 bg-card/90 backdrop-blur-sm border-border/20 shadow-lg rounded-lg p-1"
                              >
                                <DropdownMenuItem 
                                  onClick={() => openEditDialog(task)}
                                  className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted/50 hover:text-foreground transition-colors duration-150 cursor-pointer text-sm"
                                >
                                  <Edit className="h-3 w-3 text-muted-foreground" />
                                  <span>Edit Task</span>
                                </DropdownMenuItem>
                                <DropdownMenuSeparator className="my-1 bg-border/10" />
                                <DropdownMenuItem 
                                  onClick={() => openDeleteDialog(task)}
                                  className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-destructive/5 hover:text-destructive transition-colors duration-150 cursor-pointer text-sm"
                                >
                                  <Trash2 className="h-3 w-3 text-destructive/70" />
                                  <span className="text-destructive/80">Delete Task</span>
                                </DropdownMenuItem>
                              </DropdownMenuContent>
                            </DropdownMenu>
                          </div>
                        </div>
                      </CardHeader>
                      <CardContent className="pb-2 pt-1">
                        <p className="text-xs text-muted-foreground line-clamp-2 mb-3">{task.description}</p>
                        <div className="flex items-center justify-between">
                          <span className="text-xs text-muted-foreground">
                            {task.maxAttempts || '∞'} attempts
                          </span>
                          <div className="flex items-center gap-1">
                            <Button
                              variant={task.started ? "outline" : "default"}
                              size="sm"
                              onClick={() => handleStartTask(task.physicalId)}
                              disabled={task.started || startTaskMutation.isPending || !task.active}
                              className="h-6 px-2 text-xs"
                            >
                              {task.started ? 'Started' : 'Start'}
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleDeactivateTask(task.physicalId)}
                              disabled={!task.active || deactivateTaskMutation.isPending}
                              className="h-6 px-2 text-xs"
                            >
                              {task.active ? 'Deactivate' : 'Inactive'}
                            </Button>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-between pt-4">
                  <div className="text-sm text-muted-foreground">
                    Showing {((currentPage - 1) * tasksPerPage) + 1} to {Math.min(currentPage * tasksPerPage, filteredAndSortedTasks.length)} of {filteredAndSortedTasks.length} tasks
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                      disabled={currentPage === 1}
                      className="h-8 px-3"
                    >
                      Previous
                    </Button>
                    <div className="flex items-center gap-1">
                      {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                        const pageNum = Math.max(1, Math.min(totalPages - 4, currentPage - 2)) + i;
                        if (pageNum > totalPages) return null;
                        return (
                          <Button
                            key={pageNum}
                            variant={currentPage === pageNum ? 'default' : 'outline'}
                            size="sm"
                            onClick={() => setCurrentPage(pageNum)}
                            className={`h-8 w-8 p-0 ${
                              currentPage === pageNum 
                                ? 'gradient-primary text-white' 
                                : 'bg-card/90 backdrop-blur-xl border-border/50 hover:bg-card/95'
                            }`}
                          >
                            {pageNum}
                          </Button>
                        );
                      })}
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                      disabled={currentPage === totalPages}
                      className="h-8 px-3"
                    >
                      Next
                    </Button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
