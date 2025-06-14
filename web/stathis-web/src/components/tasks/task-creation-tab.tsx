'use client';

import { useState } from 'react';
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
import { ClipboardList } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getClassroomTasks, startTask, deactivateTask, deleteTask, updateTask } from '@/services/tasks/api-task-client';
import { getAllLessonTemplates, getAllQuizTemplates, getAllExerciseTemplates } from '@/services/templates/api-template-client';
import { TaskResponseDTO } from '@/services/tasks/api-task-client';
import { CreateTaskForm } from './create-task-form';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { format } from 'date-fns';
import { toast } from 'sonner';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Loader2, Plus } from 'lucide-react';
import { TemplateCreationModal } from '../templates/template-creation-modal';

interface TaskCreationTabProps {
  classroomId: string;
}

export function TaskCreationTab({ classroomId }: TaskCreationTabProps) {
  const [creatingTask, setCreatingTask] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<TaskResponseDTO | null>(null);
  
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
  };

  const handleCancelCreation = () => {
    setCreatingTask(false);
  };

  const handleTaskCreated = () => {
    setCreatingTask(false);
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
  
  // Helper function for template creation
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
    if (!task) return <Badge variant="outline">Unknown</Badge>;
    
    // Determine status based on active and started flags
    if (task.active && task.started) {
      return <Badge variant="default">Active</Badge>;
    } else if (task.active && !task.started) {
      return <Badge variant="secondary">Not Started</Badge>;
    } else {
      return <Badge variant="outline">Inactive</Badge>;
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

  return (
    <div className="space-y-6">
      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. This will permanently delete the task
              "{selectedTask?.name}" and remove it from the classroom.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleDeleteTask}
              className="bg-red-500 hover:bg-red-600"
            >
              {deleteTaskMutation.isPending ? (
                <span className="flex items-center">
                  <span className="mr-1 h-3 w-3 animate-spin rounded-full border-2 border-r-transparent"></span>
                  Deleting...
                </span>
              ) : 'Delete'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
      
      {/* Edit Task Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>Edit Task</DialogTitle>
            <DialogDescription>
              Update task details below.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <label htmlFor="taskName" className="text-sm font-medium">Task Name</label>
              <Input
                id="taskName"
                value={editTaskName}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditTaskName(e.target.value)}
                className="w-full"
              />
            </div>
            
            <div className="grid gap-2">
              <label htmlFor="taskDescription" className="text-sm font-medium">Description</label>
              <Textarea
                id="taskDescription"
                value={editTaskDescription}
                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setEditTaskDescription(e.target.value)}
                className="min-h-[100px] w-full"
              />
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div className="grid gap-2">
                <label htmlFor="taskSubmissionDate" className="text-sm font-medium">Submission Date</label>
                <Input
                  id="taskSubmissionDate"
                  type="datetime-local"
                  value={editTaskSubmissionDate ? editTaskSubmissionDate.substring(0, 16) : ''}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                    const date = new Date(e.target.value);
                    setEditTaskSubmissionDate(date.toISOString());
                  }}
                  className="w-full"
                />
              </div>
              
              <div className="grid gap-2">
                <label htmlFor="taskClosingDate" className="text-sm font-medium">Closing Date</label>
                <Input
                  id="taskClosingDate"
                  type="datetime-local"
                  value={editTaskClosingDate ? editTaskClosingDate.substring(0, 16) : ''}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                    const date = new Date(e.target.value);
                    setEditTaskClosingDate(date.toISOString());
                  }}
                  className="w-full"
                />
              </div>
            </div>
            
            <div className="grid gap-2">
              <label htmlFor="taskMaxAttempts" className="text-sm font-medium">Max Attempts</label>
              <Input
                id="taskMaxAttempts"
                type="number"
                min={0}
                value={editTaskMaxAttempts?.toString() || ''}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditTaskMaxAttempts(e.target.value ? parseInt(e.target.value) : undefined)}
                className="w-full"
              />
            </div>
            
            {/* Template Type Selection */}
            <div className="grid gap-2">
              <label htmlFor="templateType" className="text-sm font-medium">Template Type</label>
              <Select 
                onValueChange={handleTemplateTypeChange}
                value={selectedTemplateType || undefined}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select template type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LESSON">Lesson</SelectItem>
                  <SelectItem value="QUIZ">Quiz</SelectItem>
                  <SelectItem value="EXERCISE">Exercise</SelectItem>
                </SelectContent>
              </Select>
              <div className="text-xs text-muted-foreground">
                Type of content to assign to students
              </div>
            </div>
            
            {/* Template Selection */}
            {selectedTemplateType && (
              <div className="grid gap-2">
                <div className="flex justify-between items-center">
                  <label htmlFor="templateId" className="text-sm font-medium">Template</label>
                  <TemplateCreationModal 
                    templateType={selectedTemplateType as 'LESSON' | 'QUIZ' | 'EXERCISE'} 
                    onTemplateCreated={handleTemplateCreated}
                    trigger={
                      <Button variant="ghost" size="sm" className="h-8 px-2">
                        <Plus className="h-4 w-4 mr-1" />
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
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditDialogOpen(false)}>Cancel</Button>
            <Button 
              onClick={handleUpdateTask}
              disabled={updateTaskMutation.isPending}
            >
              {updateTaskMutation.isPending ? (
                <span className="flex items-center">
                  <span className="mr-1 h-3 w-3 animate-spin rounded-full border-2 border-r-transparent"></span>
                  Saving...
                </span>
              ) : 'Save Changes'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold tracking-tight">Tasks</h2>
        {!creatingTask && (
          <Button
            onClick={handleCreateTask}
            className="h-9"
          >
            <Plus className="mr-2 h-4 w-4" />
            Create Task
          </Button>
        )}
      </div>

      {creatingTask ? (
        <Card>
          <CardHeader>
            <CardTitle>Create New Task</CardTitle>
            <CardDescription>
              Create a new task for students in this classroom
            </CardDescription>
          </CardHeader>
          <CardContent>
            <CreateTaskForm 
              classroomPhysicalId={classroomId} 
              onSuccess={handleTaskCreated} 
              onCancel={handleCancelCreation}
            />
          </CardContent>
        </Card>
      ) : (
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
          ) : (
            <div className="grid gap-4">
              {tasks.map((task) => (
                <Card key={task.physicalId}>
                  <CardHeader className="pb-2">
                    <div className="flex justify-between">
                      <div>
                        <CardTitle>{task.name}</CardTitle>
                        <CardDescription>
                          {getTemplateTypeLabel(getTemplateType(task))} • Due {formatDate(task.submissionDate)}
                        </CardDescription>
                      </div>
                      <div>
                        {getStatusBadge(task)}
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm">{task.description}</p>
                  </CardContent>
                  <CardFooter className="flex justify-between border-t pt-4">
                    <div className="text-sm text-muted-foreground">
                      Max Attempts: <span className="font-medium">{task.maxAttempts || 'Unlimited'}</span>
                    </div>
                    <div className="flex space-x-2">
                      {/* Start Task button - always visible but disabled based on state */}
                      <Button
                        variant={task.started ? "outline" : "default"}
                        size="sm"
                        onClick={() => handleStartTask(task.physicalId)}
                        disabled={task.started || startTaskMutation.isPending || !task.active}
                      >
                        {startTaskMutation.isPending && startTaskMutation.variables === task.physicalId ? (
                          <span className="flex items-center">
                            <span className="mr-1 h-3 w-3 animate-spin rounded-full border-2 border-r-transparent"></span>
                            Starting...
                          </span>
                        ) : task.started ? 'Started' : 'Start'}
                      </Button>
                      
                      {/* Deactivate Task button - always visible but disabled based on state */}
                      <Button
                        variant={!task.active ? "outline" : "destructive"}
                        size="sm"
                        onClick={() => handleDeactivateTask(task.physicalId)}
                        disabled={!task.active || deactivateTaskMutation.isPending}
                      >
                        {deactivateTaskMutation.isPending && deactivateTaskMutation.variables === task.physicalId ? (
                          <span className="flex items-center">
                            <span className="mr-1 h-3 w-3 animate-spin rounded-full border-2 border-r-transparent"></span>
                            Deactivating...
                          </span>
                        ) : !task.active ? 'Inactive' : 'Deactivate'}
                      </Button>
                      
                      {/* Edit Task button */}
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => openEditDialog(task)}
                      >
                        Edit
                      </Button>
                      
                      {/* Delete Task button */}
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => openDeleteDialog(task)}
                        className="text-red-500 hover:text-red-700"
                      >
                        {deleteTaskMutation.isPending && deleteTaskMutation.variables === task.physicalId ? (
                          <span className="flex items-center">
                            <span className="mr-1 h-3 w-3 animate-spin rounded-full border-2 border-r-transparent"></span>
                            Deleting...
                          </span>
                        ) : 'Delete'}
                      </Button>
                    </div>
                  </CardFooter>
                </Card>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
