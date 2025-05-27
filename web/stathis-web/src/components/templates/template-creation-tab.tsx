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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { BookOpen, BookText, Activity, Plus } from 'lucide-react';
import { CreateLessonForm } from './create-lesson-form';
import { CreateQuizForm } from './create-quiz-form';
import { CreateExerciseForm } from './create-exercise-form';

type TemplateType = 'lesson' | 'quiz' | 'exercise' | null;

interface TemplateCreationTabProps {
  classroomId: string;
}

export function TemplateCreationTab({ classroomId }: TemplateCreationTabProps) {
  const [activeTab, setActiveTab] = useState<string>('templates');
  const [creatingTemplateType, setCreatingTemplateType] = useState<TemplateType>(null);

  const handleCreateTemplate = (type: TemplateType) => {
    setCreatingTemplateType(type);
  };

  const handleCancelCreation = () => {
    setCreatingTemplateType(null);
  };

  const handleTemplateCreated = () => {
    setCreatingTemplateType(null);
    // Optionally: refresh template list
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold tracking-tight">Templates</h2>
        {!creatingTemplateType && (
          <div className="flex space-x-2">
            <Button
              variant="outline"
              size="sm"
              className="h-9"
              onClick={() => handleCreateTemplate('lesson')}
            >
              <BookOpen className="mr-2 h-4 w-4" />
              Create Lesson
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="h-9"
              onClick={() => handleCreateTemplate('quiz')}
            >
              <BookText className="mr-2 h-4 w-4" />
              Create Quiz
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="h-9"
              onClick={() => handleCreateTemplate('exercise')}
            >
              <Activity className="mr-2 h-4 w-4" />
              Create Exercise
            </Button>
          </div>
        )}
      </div>

      {creatingTemplateType ? (
        <Card>
          <CardHeader>
            <CardTitle>
              {creatingTemplateType === 'lesson' && 'Create Lesson Template'}
              {creatingTemplateType === 'quiz' && 'Create Quiz Template'}
              {creatingTemplateType === 'exercise' && 'Create Exercise Template'}
            </CardTitle>
            <CardDescription>
              {creatingTemplateType === 'lesson' && 'Create a new lesson template for your classroom'}
              {creatingTemplateType === 'quiz' && 'Create a new quiz template for your classroom'}
              {creatingTemplateType === 'exercise' && 'Create a new exercise template for your classroom'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {creatingTemplateType === 'lesson' && (
              <CreateLessonForm 
                onSuccess={handleTemplateCreated} 
                onCancel={handleCancelCreation}
              />
            )}
            {creatingTemplateType === 'quiz' && (
              <CreateQuizForm 
                onSuccess={handleTemplateCreated} 
                onCancel={handleCancelCreation}
              />
            )}
            {creatingTemplateType === 'exercise' && (
              <CreateExerciseForm 
                onSuccess={handleTemplateCreated} 
                onCancel={handleCancelCreation}
              />
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card className="col-span-1">
            <CardHeader className="pb-3">
              <CardTitle className="text-lg">Lesson Templates</CardTitle>
              <CardDescription>
                Educational content for students
              </CardDescription>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              <p>No lesson templates yet</p>
            </CardContent>
            <CardFooter>
              <Button 
                variant="ghost" 
                className="w-full justify-start text-sm" 
                onClick={() => handleCreateTemplate('lesson')}
              >
                <Plus className="mr-2 h-4 w-4" />
                Create Lesson Template
              </Button>
            </CardFooter>
          </Card>

          <Card className="col-span-1">
            <CardHeader className="pb-3">
              <CardTitle className="text-lg">Quiz Templates</CardTitle>
              <CardDescription>
                Assessments to test knowledge
              </CardDescription>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              <p>No quiz templates yet</p>
            </CardContent>
            <CardFooter>
              <Button 
                variant="ghost" 
                className="w-full justify-start text-sm" 
                onClick={() => handleCreateTemplate('quiz')}
              >
                <Plus className="mr-2 h-4 w-4" />
                Create Quiz Template
              </Button>
            </CardFooter>
          </Card>

          <Card className="col-span-1">
            <CardHeader className="pb-3">
              <CardTitle className="text-lg">Exercise Templates</CardTitle>
              <CardDescription>
                Physical activities for students
              </CardDescription>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              <p>No exercise templates yet</p>
            </CardContent>
            <CardFooter>
              <Button 
                variant="ghost" 
                className="w-full justify-start text-sm" 
                onClick={() => handleCreateTemplate('exercise')}
              >
                <Plus className="mr-2 h-4 w-4" />
                Create Exercise Template
              </Button>
            </CardFooter>
          </Card>
        </div>
      )}
    </div>
  );
}
