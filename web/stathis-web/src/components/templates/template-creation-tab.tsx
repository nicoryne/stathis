'use client';

import { useState } from 'react';
import { motion } from 'framer-motion';
import { LessonTemplateResponseDTO, QuizTemplateResponseDTO, ExerciseTemplateResponseDTO } from '@/services/templates/api-template';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { BookOpen, BookText, Activity, Plus, Eye, Loader2 } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { 
  getLessonTemplate, 
  getQuizTemplate, 
  getExerciseTemplate,
  getTeacherLessonTemplates,
  getTeacherQuizTemplates,
  getTeacherExerciseTemplates,
  deleteLessonTemplate,
  deleteQuizTemplate,
  deleteExerciseTemplate
} from '@/services/templates/api-template-client';
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
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | null>(null);
  const [selectedTemplateType, setSelectedTemplateType] = useState<TemplateType>(null);
  const [isReviewDialogOpen, setIsReviewDialogOpen] = useState(false);

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
  
  const handleSelectTemplate = (templateId: string, type: TemplateType) => {
    setSelectedTemplateId(templateId);
    setSelectedTemplateType(type);
  };
  
  const handleReviewTemplate = () => {
    if (selectedTemplateId && selectedTemplateType) {
      setIsReviewDialogOpen(true);
    }
  };
  
  // Query to fetch template details when reviewing
  // Get lesson templates created by the current teacher
  const {
    data: lessonTemplates,
    isLoading: isLoadingLessonTemplates,
    refetch: refetchLessonTemplates
  } = useQuery({
    queryKey: ['teacher-lesson-templates'],
    queryFn: getTeacherLessonTemplates,
  });

  // Get quiz templates created by the current teacher
  const {
    data: quizTemplates,
    isLoading: isLoadingQuizTemplates,
    refetch: refetchQuizTemplates
  } = useQuery({
    queryKey: ['teacher-quiz-templates'],
    queryFn: getTeacherQuizTemplates,
  });

  // Get exercise templates created by the current teacher
  const {
    data: exerciseTemplates,
    isLoading: isLoadingExerciseTemplates,
    refetch: refetchExerciseTemplates
  } = useQuery({
    queryKey: ['teacher-exercise-templates'],
    queryFn: getTeacherExerciseTemplates,
  });
  
  // Query for the selected template details
  const {
    data: templateDetails,
    isLoading: isLoadingTemplate,
    error: templateError
  } = useQuery({
    queryKey: ['template-details', selectedTemplateType, selectedTemplateId],
    queryFn: async () => {
      if (!selectedTemplateId || !selectedTemplateType) return null;
      
      switch (selectedTemplateType) {
        case 'lesson':
          return await getLessonTemplate(selectedTemplateId);
        case 'quiz':
          return await getQuizTemplate(selectedTemplateId);
        case 'exercise':
          return await getExerciseTemplate(selectedTemplateId);
        default:
          return null;
      }
    },
    enabled: !!selectedTemplateId && !!selectedTemplateType && isReviewDialogOpen,
  });

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
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <Card className="max-h-[80vh] flex flex-col rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300">
            <CardHeader className="sticky top-0 z-10 bg-card/80 backdrop-blur-xl border-b border-border/50">
              <CardTitle className="flex items-center gap-3">
                <div className="p-2 rounded-full bg-primary/10">
                  {creatingTemplateType === 'lesson' && <BookOpen className="h-5 w-5 text-primary" />}
                  {creatingTemplateType === 'quiz' && <BookText className="h-5 w-5 text-primary" />}
                  {creatingTemplateType === 'exercise' && <Activity className="h-5 w-5 text-primary" />}
                </div>
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
            <CardContent className="overflow-y-auto flex-1 p-6">
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
        </motion.div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
          >
            <Card className="col-span-1 rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300">
              <CardHeader className="pb-3">
                <CardTitle className="text-lg flex items-center gap-3">
                  <div className="p-2 rounded-full bg-primary/10">
                    <BookOpen className="h-5 w-5 text-primary" />
                  </div>
                  Lesson Templates
                </CardTitle>
                <CardDescription>
                  Educational content for students
                </CardDescription>
              </CardHeader>
              <CardContent className="text-sm">
                {isLoadingLessonTemplates ? (
                  <div className="flex justify-center py-4">
                    <div className="flex flex-col items-center gap-2">
                      <div className="relative">
                        <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg animate-pulse" />
                        <Loader2 className="relative h-4 w-4 animate-spin text-primary" />
                      </div>
                      <span className="text-muted-foreground text-xs">Loading...</span>
                    </div>
                  </div>
                ) : !lessonTemplates || lessonTemplates.length === 0 ? (
                  <div className="text-center py-4">
                    <div className="relative mx-auto w-12 h-12 mb-2">
                      <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg" />
                      <BookOpen className="relative mx-auto h-12 w-12 text-muted-foreground" />
                    </div>
                    <p className="text-muted-foreground">No lesson templates yet</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {lessonTemplates.map((template) => (
                      <div 
                        key={template.physicalId} 
                        className={`p-3 rounded-xl cursor-pointer border transition-all duration-200 ${
                          selectedTemplateId === template.physicalId 
                            ? 'border-primary bg-primary/10 shadow-md' 
                            : 'border-border/50 hover:border-primary/50 hover:bg-primary/5'
                        }`}
                        onClick={() => handleSelectTemplate(template.physicalId, 'lesson')}
                      >
                        <p className="font-medium">{template.title}</p>
                        <p className="text-xs text-muted-foreground truncate">{template.description}</p>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
              <CardFooter>
                <div className="w-full space-y-2">
                  {selectedTemplateId && selectedTemplateType === 'lesson' && (
                    <Button 
                      variant="outline" 
                      className="w-full justify-start text-sm bg-card/80 backdrop-blur-xl border-border/50 hover:bg-card/90" 
                      onClick={handleReviewTemplate}
                    >
                      <Eye className="mr-2 h-4 w-4" />
                      Review Selected Template
                    </Button>
                  )}
                  <Button 
                    variant="ghost" 
                    className="w-full justify-start text-sm bg-gradient-to-r from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg hover:shadow-xl transition-all duration-200" 
                    onClick={() => handleCreateTemplate('lesson')}
                  >
                    <Plus className="mr-2 h-4 w-4" />
                    Create Lesson Template
                  </Button>
                </div>
              </CardFooter>
            </Card>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
          >
            <Card className="col-span-1 rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300">
              <CardHeader className="pb-3">
                <CardTitle className="text-lg flex items-center gap-3">
                  <div className="p-2 rounded-full bg-secondary/10">
                    <BookText className="h-5 w-5 text-secondary" />
                  </div>
                  Quiz Templates
                </CardTitle>
                <CardDescription>
                  Assessments to test knowledge
                </CardDescription>
              </CardHeader>
              <CardContent className="text-sm">
                {isLoadingQuizTemplates ? (
                  <div className="flex justify-center py-4">
                    <div className="flex flex-col items-center gap-2">
                      <div className="relative">
                        <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg animate-pulse" />
                        <Loader2 className="relative h-4 w-4 animate-spin text-secondary" />
                      </div>
                      <span className="text-muted-foreground text-xs">Loading...</span>
                    </div>
                  </div>
                ) : !quizTemplates || quizTemplates.length === 0 ? (
                  <div className="text-center py-4">
                    <div className="relative mx-auto w-12 h-12 mb-2">
                      <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-secondary/20 to-accent/20 blur-lg" />
                      <BookText className="relative mx-auto h-12 w-12 text-muted-foreground" />
                    </div>
                    <p className="text-muted-foreground">No quiz templates yet</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {quizTemplates.map((template) => (
                      <div 
                        key={template.physicalId} 
                        className={`p-3 rounded-xl cursor-pointer border transition-all duration-200 ${
                          selectedTemplateId === template.physicalId 
                            ? 'border-secondary bg-secondary/10 shadow-md' 
                            : 'border-border/50 hover:border-secondary/50 hover:bg-secondary/5'
                        }`}
                        onClick={() => handleSelectTemplate(template.physicalId, 'quiz')}
                      >
                        <p className="font-medium">{template.title}</p>
                        <p className="text-xs text-muted-foreground truncate">{template.instruction}</p>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
              <CardFooter>
                <div className="w-full space-y-2">
                  {selectedTemplateId && selectedTemplateType === 'quiz' && (
                    <Button 
                      variant="outline" 
                      className="w-full justify-start text-sm bg-card/80 backdrop-blur-xl border-border/50 hover:bg-card/90" 
                      onClick={handleReviewTemplate}
                    >
                      <Eye className="mr-2 h-4 w-4" />
                      Review Selected Template
                    </Button>
                  )}
                  <Button 
                    variant="ghost" 
                    className="w-full justify-start text-sm bg-gradient-to-r from-secondary to-accent hover:from-secondary/90 hover:to-accent/90 shadow-lg hover:shadow-xl transition-all duration-200" 
                    onClick={() => handleCreateTemplate('quiz')}
                  >
                    <Plus className="mr-2 h-4 w-4" />
                    Create Quiz Template
                  </Button>
                </div>
              </CardFooter>
            </Card>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
          >
            <Card className="col-span-1 rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300">
              <CardHeader className="pb-3">
                <CardTitle className="text-lg flex items-center gap-3">
                  <div className="p-2 rounded-full bg-accent/10">
                    <Activity className="h-5 w-5 text-accent" />
                  </div>
                  Exercise Templates
                </CardTitle>
                <CardDescription>
                  Physical activities for students
                </CardDescription>
              </CardHeader>
              <CardContent className="text-sm">
                {isLoadingExerciseTemplates ? (
                  <div className="flex justify-center py-4">
                    <div className="flex flex-col items-center gap-2">
                      <div className="relative">
                        <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-accent/20 to-primary/20 blur-lg animate-pulse" />
                        <Loader2 className="relative h-4 w-4 animate-spin text-accent" />
                      </div>
                      <span className="text-muted-foreground text-xs">Loading...</span>
                    </div>
                  </div>
                ) : !exerciseTemplates || exerciseTemplates.length === 0 ? (
                  <div className="text-center py-4">
                    <div className="relative mx-auto w-12 h-12 mb-2">
                      <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-accent/20 to-primary/20 blur-lg" />
                      <Activity className="relative mx-auto h-12 w-12 text-muted-foreground" />
                    </div>
                    <p className="text-muted-foreground">No exercise templates yet</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {exerciseTemplates.map((template) => (
                      <div 
                        key={template.physicalId} 
                        className={`p-3 rounded-xl cursor-pointer border transition-all duration-200 ${
                          selectedTemplateId === template.physicalId 
                            ? 'border-accent bg-accent/10 shadow-md' 
                            : 'border-border/50 hover:border-accent/50 hover:bg-accent/5'
                        }`}
                        onClick={() => handleSelectTemplate(template.physicalId, 'exercise')}
                      >
                        <p className="font-medium">{template.title}</p>
                        <p className="text-xs text-muted-foreground truncate">{template.description}</p>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
              <CardFooter>
                <div className="w-full space-y-2">
                  {selectedTemplateId && selectedTemplateType === 'exercise' && (
                    <Button 
                      variant="outline" 
                      className="w-full justify-start text-sm bg-card/80 backdrop-blur-xl border-border/50 hover:bg-card/90" 
                      onClick={handleReviewTemplate}
                    >
                      <Eye className="mr-2 h-4 w-4" />
                      Review Selected Template
                    </Button>
                  )}
                  <Button 
                    variant="ghost" 
                    className="w-full justify-start text-sm bg-gradient-to-r from-accent to-primary hover:from-accent/90 hover:to-primary/90 shadow-lg hover:shadow-xl transition-all duration-200" 
                    onClick={() => handleCreateTemplate('exercise')}
                  >
                    <Plus className="mr-2 h-4 w-4" />
                    Create Exercise Template
                  </Button>
                </div>
              </CardFooter>
            </Card>
          </motion.div>
        </div>
      )}
      {/* Template Review Dialog */}
      <Dialog open={isReviewDialogOpen} onOpenChange={setIsReviewDialogOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>
              {selectedTemplateType === 'lesson' && 'Lesson Template Details'}
              {selectedTemplateType === 'quiz' && 'Quiz Template Details'}
              {selectedTemplateType === 'exercise' && 'Exercise Template Details'}
            </DialogTitle>
            <DialogDescription>
              Review the template details before using it
            </DialogDescription>
          </DialogHeader>
          
          {isLoadingTemplate ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-8 w-8 animate-spin" />
            </div>
          ) : templateError ? (
            <div className="text-center py-4 text-destructive">
              Error loading template. Please try again.
            </div>
          ) : templateDetails ? (
            <div className="max-h-[60vh] overflow-y-auto p-4 space-y-4">
              {/* Template details display */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Title</h3>
                  <p className="text-lg font-medium">{templateDetails.title || 'No title'}</p>
                </div>

                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Template ID</h3>
                  <p className="font-mono text-xs">{selectedTemplateId}</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <h3 className="text-sm font-medium text-muted-foreground">Description</h3>
                <p className="whitespace-pre-wrap">
                  {selectedTemplateType === 'quiz' 
                    ? (templateDetails as QuizTemplateResponseDTO).instruction 
                    : selectedTemplateType === 'lesson' 
                      ? (templateDetails as LessonTemplateResponseDTO).description 
                      : (templateDetails as ExerciseTemplateResponseDTO).description || 'No description available'}
                </p>
              </div>
              
              {/* Render template-specific content */}
              {selectedTemplateType === 'lesson' && (
                <div className="space-y-2">
                  <h3 className="text-sm font-medium text-muted-foreground">Content</h3>
                  <div className="p-4 border rounded-md bg-muted/30">
                    <div className="whitespace-pre-wrap">{(templateDetails as any).content || 'No content available'}</div>
                  </div>
                </div>
              )}
              
              {selectedTemplateType === 'quiz' && (
                <div className="space-y-4">
                  <h3 className="text-sm font-medium text-muted-foreground">Questions</h3>
                  {(templateDetails as any).questions?.length ? (
                    <div className="space-y-4">
                      {(templateDetails as any).questions.map((q: any, i: number) => (
                        <div key={i} className="p-4 border rounded-md bg-muted/30">
                          <p className="font-medium">Question {i + 1}: {q.text}</p>
                          <div className="mt-2 space-y-1">
                            {q.options?.map((opt: any, j: number) => (
                              <div key={j} className="flex items-center gap-2">
                                <div className={`w-4 h-4 rounded-full ${opt.isCorrect ? 'bg-green-500' : 'bg-gray-300'}`}></div>
                                <p>{opt.text}</p>
                              </div>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p>No questions available</p>
                  )}
                </div>
              )}
              
              {selectedTemplateType === 'exercise' && (
                <div className="space-y-2">
                  <h3 className="text-sm font-medium text-muted-foreground">Exercise Details</h3>
                  <div className="p-4 border rounded-md bg-muted/30">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <h4 className="text-xs font-medium text-muted-foreground">Duration</h4>
                        <p>{(templateDetails as any).duration || 'Not specified'} minutes</p>
                      </div>
                      <div>
                        <h4 className="text-xs font-medium text-muted-foreground">Intensity</h4>
                        <p>{(templateDetails as any).intensity || 'Not specified'}</p>
                      </div>
                      <div className="md:col-span-2">
                        <h4 className="text-xs font-medium text-muted-foreground">Instructions</h4>
                        <p className="whitespace-pre-wrap">{(templateDetails as any).instructions || 'No instructions available'}</p>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="text-center py-4 text-muted-foreground">
              No template details available
            </div>
          )}
          
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsReviewDialogOpen(false)}>Close</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
