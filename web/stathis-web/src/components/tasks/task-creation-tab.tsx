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
import { ClipboardList, Plus } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { getClassroomTasks } from '@/services/tasks/api-task-client';
import { CreateTaskForm } from './create-task-form';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { format } from 'date-fns';

interface TaskCreationTabProps {
  classroomId: string;
}

export function TaskCreationTab({ classroomId }: TaskCreationTabProps) {
  const [creatingTask, setCreatingTask] = useState(false);

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

  // Helper function to format date
  const formatDate = (dateString: string) => {
    try {
      return format(new Date(dateString), 'PPP');
    } catch (e) {
      return 'Invalid date';
    }
  };

  // Helper function to get status badge variant
  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge variant="default">Active</Badge>;
      case 'INACTIVE':
        return <Badge variant="secondary">Inactive</Badge>;
      default:
        return <Badge variant="outline">Unknown</Badge>;
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
                        <CardTitle>{task.title}</CardTitle>
                        <CardDescription>
                          {getTemplateTypeLabel(task.templateType)} • Due {formatDate(task.dueDate)}
                        </CardDescription>
                      </div>
                      <div>
                        {getStatusBadge(task.status)}
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm">{task.description}</p>
                  </CardContent>
                  <CardFooter className="flex justify-between border-t pt-4">
                    <div className="text-sm text-muted-foreground">
                      Points: <span className="font-medium">{task.points}</span>
                    </div>
                    <div className="flex space-x-2">
                      <Button variant="outline" size="sm">View</Button>
                      <Button variant="outline" size="sm">Edit</Button>
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
