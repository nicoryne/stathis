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
  continueToTask?: boolean; // If true, will keep the modal open for task creation, if false will just close
}

export function TemplateCreationModal({ 
  templateType = null, 
  onTemplateCreated,
  trigger,
  continueToTask = false // Default to false - just create template and close
}: TemplateCreationModalProps) {
  const [open, setOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<TemplateType>(
    templateType === 'LESSON' ? 'lesson' : 
    templateType === 'QUIZ' ? 'quiz' : 
    templateType === 'EXERCISE' ? 'exercise' : 
    'lesson'
  );

  const handleSuccess = () => {
    // Call the onTemplateCreated callback to notify parent component
    onTemplateCreated();
    
    // Always close the modal after successful template creation
    setOpen(false);
    
    // Make sure we stay on the templates tab if we're not in a task context
    if (typeof window !== 'undefined' && !window.location.hash.includes('tasks')) {
      // Set the hash to 'templates' to ensure we stay on that tab
      window.location.hash = 'templates';
    }
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
      <DialogContent className="sm:max-w-[800px] max-h-[90vh] flex flex-col p-0 gap-0">
        <DialogHeader className="p-6 pb-4 sticky top-0 z-10 bg-background">
          <DialogTitle>Create New Template</DialogTitle>
          <DialogDescription className="pb-2">
            Create a new template to use in your tasks
          </DialogDescription>
        </DialogHeader>
        
        <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as TemplateType)} className="w-full flex-1 flex flex-col overflow-hidden">
          <div className="px-6 pb-2 pt-1 sticky z-10 bg-background">
            <TabsList className="grid grid-cols-3 w-full">
              <TabsTrigger value="lesson">Lesson</TabsTrigger>
              <TabsTrigger value="quiz">Quiz</TabsTrigger>
              <TabsTrigger value="exercise">Exercise</TabsTrigger>
            </TabsList>
          </div>
          
          <div className="flex-1 overflow-y-auto p-6 pt-4">
            <TabsContent value="lesson" className="h-full mt-0 data-[state=active]:flex data-[state=active]:flex-col">
              <CreateLessonForm 
                onSuccess={handleSuccess}
                onCancel={() => setOpen(false)}
              />
            </TabsContent>
            
            <TabsContent value="quiz" className="h-full mt-0 data-[state=active]:flex data-[state=active]:flex-col">
              <CreateQuizForm 
                onSuccess={handleSuccess}
                onCancel={() => setOpen(false)}
              />
            </TabsContent>
            
            <TabsContent value="exercise" className="h-full mt-0 data-[state=active]:flex data-[state=active]:flex-col">
              <CreateExerciseForm 
                onSuccess={handleSuccess}
                onCancel={() => setOpen(false)}
              />
            </TabsContent>
          </div>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
}
