'use client';

import { useState } from 'react';
import { Plus } from 'lucide-react';
import { motion } from 'framer-motion';
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
      <DialogContent className="sm:max-w-[900px] max-h-[90vh] flex flex-col p-0 gap-0 bg-card/98 backdrop-blur-xl border-border/30 shadow-2xl rounded-3xl overflow-hidden">
        <DialogHeader className="p-8 pb-6 sticky top-0 z-10 bg-gradient-to-r from-background/98 to-card/98 backdrop-blur-xl border-b border-border/20">
          <motion.div
            initial={{ opacity: 0, y: -15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
            className="flex items-center gap-5"
          >
            <div className="relative">
              <div className="absolute -inset-3 rounded-full bg-gradient-to-r from-primary/25 to-secondary/25 blur-xl" />
              <div className="relative w-12 h-12 rounded-full bg-gradient-to-r from-primary to-secondary flex items-center justify-center shadow-xl">
                <Plus className="h-6 w-6 text-white" />
              </div>
            </div>
            <div className="flex-1">
              <DialogTitle className="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent leading-tight">
                Create New Template
              </DialogTitle>
              <DialogDescription className="text-muted-foreground mt-2 text-base">
                Create a new template to use in your tasks
              </DialogDescription>
            </div>
          </motion.div>
        </DialogHeader>
        
        <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as TemplateType)} className="w-full flex-1 flex flex-col overflow-hidden">
          <div className="px-8 pb-4 pt-2 sticky z-10 bg-gradient-to-r from-background/98 to-card/98 backdrop-blur-xl">
            <TabsList className="grid grid-cols-3 w-full bg-muted/40 backdrop-blur-sm border border-border/30 rounded-2xl p-2 h-14">
              <TabsTrigger 
                value="lesson" 
                className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-primary data-[state=active]:to-secondary data-[state=active]:text-white rounded-xl transition-all duration-300 text-sm font-medium h-10"
              >
                📚 Lesson
              </TabsTrigger>
              <TabsTrigger 
                value="quiz" 
                className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-secondary data-[state=active]:to-accent data-[state=active]:text-white rounded-xl transition-all duration-300 text-sm font-medium h-10"
              >
                📝 Quiz
              </TabsTrigger>
              <TabsTrigger 
                value="exercise" 
                className="data-[state=active]:bg-gradient-to-r data-[state=active]:from-accent data-[state=active]:to-primary data-[state=active]:text-white rounded-xl transition-all duration-300 text-sm font-medium h-10"
              >
                💪 Exercise
              </TabsTrigger>
            </TabsList>
          </div>
          
          <div className="flex-1 overflow-y-auto p-8 pt-6 bg-gradient-to-b from-background/40 to-card/20 custom-scrollbar">
            <TabsContent value="lesson" className="h-full mt-0 data-[state=active]:flex data-[state=active]:flex-col">
              <motion.div
                initial={{ opacity: 0, y: 25 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
                className="flex-1"
              >
                <CreateLessonForm 
                  onSuccess={handleSuccess}
                  onCancel={() => setOpen(false)}
                />
              </motion.div>
            </TabsContent>
            
            <TabsContent value="quiz" className="h-full mt-0 data-[state=active]:flex data-[state=active]:flex-col">
              <motion.div
                initial={{ opacity: 0, y: 25 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
                className="flex-1"
              >
                <CreateQuizForm 
                  onSuccess={handleSuccess}
                  onCancel={() => setOpen(false)}
                />
              </motion.div>
            </TabsContent>
            
            <TabsContent value="exercise" className="h-full mt-0 data-[state=active]:flex data-[state=active]:flex-col">
              <motion.div
                initial={{ opacity: 0, y: 25 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
                className="flex-1"
              >
                <CreateExerciseForm 
                  onSuccess={handleSuccess}
                  onCancel={() => setOpen(false)}
                />
              </motion.div>
            </TabsContent>
          </div>
        </Tabs>
      </DialogContent>
    </Dialog>
  );
}
