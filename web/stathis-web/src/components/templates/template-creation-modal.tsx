'use client';

import { useState } from 'react';
import { Plus } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "@/components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from '@/components/ui/button';
import { CreateLessonForm } from './create-lesson-form';
import { CreateQuizForm } from './create-quiz-form';
import { CreateExerciseForm } from './create-exercise-form';

type TemplateType = 'lesson' | 'quiz' | 'exercise';

interface TemplateCreationModalProps {
  templateType?: 'LESSON' | 'QUIZ' | 'EXERCISE' | null;
  onTemplateCreated: () => void;
  trigger?: React.ReactNode;
}

export function TemplateCreationModal({ 
  templateType = null, 
  onTemplateCreated,
  trigger 
}: TemplateCreationModalProps) {
  const [open, setOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<TemplateType>(
    templateType === 'LESSON' ? 'lesson' : 
    templateType === 'QUIZ' ? 'quiz' : 
    templateType === 'EXERCISE' ? 'exercise' : 
    'lesson'
  );

  const handleSuccess = () => {
    onTemplateCreated();
    setOpen(false);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger || (
          <Button variant="outline" size="sm">
            <Plus className="mr-2 h-4 w-4" />
            Create Template
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[800px]">
        <DialogHeader>
          <DialogTitle>Create New Template</DialogTitle>
          <DialogDescription>
            Create a new template to use in your tasks
          </DialogDescription>
        </DialogHeader>
        
        <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as TemplateType)} className="w-full mt-4">
          <TabsList className="grid grid-cols-3 mb-6">
            <TabsTrigger value="lesson">Lesson</TabsTrigger>
            <TabsTrigger value="quiz">Quiz</TabsTrigger>
            <TabsTrigger value="exercise">Exercise</TabsTrigger>
          </TabsList>
          
          <TabsContent value="lesson" className="space-y-4">
            <CreateLessonForm 
              onSuccess={handleSuccess}
              onCancel={() => setOpen(false)}
            />
          </TabsContent>
          
          <TabsContent value="quiz" className="space-y-4">
            <CreateQuizForm 
              onSuccess={handleSuccess}
              onCancel={() => setOpen(false)}
            />
          </TabsContent>
          
          <TabsContent value="exercise" className="space-y-4">
            <CreateExerciseForm 
              onSuccess={handleSuccess}
              onCancel={() => setOpen(false)}
            />
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
}
