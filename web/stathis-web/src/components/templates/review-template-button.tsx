'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Eye } from 'lucide-react';
import { motion } from 'framer-motion';
import { 
  Dialog, 
  DialogContent, 
  DialogHeader, 
  DialogTitle,
  DialogDescription
} from '@/components/ui/dialog';
import { 
  getLessonTemplate, 
  getQuizTemplate, 
  getExerciseTemplate 
} from '@/services/templates/api-template-client';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';

// Type for template types
type TemplateType = 'lesson' | 'quiz' | 'exercise';

interface ReviewTemplateButtonProps {
  templateId?: string;
  templateType: TemplateType;
  disabled?: boolean;
}

export function ReviewTemplateButton({ 
  templateId, 
  templateType,
  disabled = false
}: ReviewTemplateButtonProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [templateData, setTemplateData] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  
  const fetchTemplate = async () => {
    if (!templateId) {
      toast.error('No template ID provided');
      return;
    }
    
    setIsLoading(true);
    setError(null);
    
    try {
      let data;
      
      switch (templateType) {
        case 'lesson':
          data = await getLessonTemplate(templateId);
          break;
        case 'quiz':
          data = await getQuizTemplate(templateId);
          break;
        case 'exercise':
          data = await getExerciseTemplate(templateId);
          break;
      }
      
      setTemplateData(data);
      setIsOpen(true);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load template';
      setError(errorMessage);
      toast.error('Failed to load template', {
        description: 'Please try again later'
      });
      console.error('Error fetching template:', err);
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <>
      <Button
        type="button"
        variant="outline"
        size="sm"
        className="gap-1"
        onClick={fetchTemplate}
        disabled={disabled || isLoading || !templateId}
      >
        {isLoading ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" />
            Loading...
          </>
        ) : (
          <>
            <Eye className="h-4 w-4" />
            Review Template
          </>
        )}
      </Button>
      
      <Dialog open={isOpen} onOpenChange={setIsOpen}>
        <DialogContent className="max-w-4xl max-h-[85vh] flex flex-col p-0 gap-0 bg-card/98 backdrop-blur-xl border-border/30 shadow-2xl rounded-3xl overflow-hidden">
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
                  <Eye className="h-6 w-6 text-white" />
                </div>
              </div>
              <div className="flex-1">
                <DialogTitle className="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent leading-tight">
                  {templateType.charAt(0).toUpperCase() + templateType.slice(1)} Template Review
                </DialogTitle>
                <DialogDescription className="text-muted-foreground mt-2 text-base">
                  Reviewing template: {templateId}
                </DialogDescription>
              </div>
            </motion.div>
          </DialogHeader>
          
          <div className="flex-1 overflow-y-auto p-8 pt-6 bg-gradient-to-b from-background/40 to-card/20 custom-scrollbar">
            {templateData ? (
              <motion.div
                initial={{ opacity: 0, y: 25 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
                className="space-y-8"
              >
                {/* Template Header */}
                <div className="bg-gradient-to-r from-primary/5 to-secondary/5 backdrop-blur-sm rounded-2xl p-6 border border-border/20">
                  <div className="space-y-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center">
                        <span className="text-lg">
                          {templateType === 'lesson' ? '📚' : templateType === 'quiz' ? '📝' : '💪'}
                        </span>
                      </div>
                      <div>
                        <h3 className="text-xl font-bold text-foreground">{templateData.title}</h3>
                        <p className="text-sm text-muted-foreground">{templateData.description || templateData.instruction}</p>
                      </div>
                    </div>
                  </div>
                </div>
                
                {/* Template-specific content rendering */}
                {templateType === 'lesson' && (
                  <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
                    <h4 className="text-lg font-semibold mb-4 flex items-center gap-2">
                      <span className="w-6 h-6 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center text-xs font-medium">
                        📖
                      </span>
                      Lesson Content
                    </h4>
                    <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                      <div className="prose prose-sm max-w-none">
                        <pre className="text-sm overflow-auto p-4 bg-muted/50 rounded-lg whitespace-pre-wrap">
                          {JSON.stringify(JSON.parse(templateData.content), null, 2)}
                        </pre>
                      </div>
                    </div>
                  </div>
                )}
                
                {templateType === 'quiz' && (
                  <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
                    <h4 className="text-lg font-semibold mb-4 flex items-center gap-2">
                      <span className="w-6 h-6 rounded-full bg-gradient-to-r from-secondary/20 to-accent/20 flex items-center justify-center text-xs font-medium">
                        ❓
                      </span>
                      Quiz Details
                    </h4>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                      <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                        <span className="text-sm font-medium text-muted-foreground">Max Score:</span>
                        <p className="text-lg font-semibold text-primary">{templateData.maxScore}</p>
                      </div>
                    </div>
                    <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                      <h5 className="font-medium mb-3">Questions</h5>
                      <div className="prose prose-sm max-w-none">
                        <pre className="text-sm overflow-auto p-4 bg-muted/50 rounded-lg whitespace-pre-wrap">
                          {JSON.stringify(JSON.parse(templateData.content), null, 2)}
                        </pre>
                      </div>
                    </div>
                  </div>
                )}
                
                {templateType === 'exercise' && (
                  <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
                    <h4 className="text-lg font-semibold mb-4 flex items-center gap-2">
                      <span className="w-6 h-6 rounded-full bg-gradient-to-r from-accent/20 to-primary/20 flex items-center justify-center text-xs font-medium">
                        💪
                      </span>
                      Exercise Details
                    </h4>
                    <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                      <div className="prose prose-sm max-w-none">
                        <pre className="text-sm overflow-auto p-4 bg-muted/50 rounded-lg whitespace-pre-wrap">
                          {JSON.stringify(JSON.parse(templateData.content), null, 2)}
                        </pre>
                      </div>
                    </div>
                  </div>
                )}
                
                {/* Template Metadata */}
                <div className="bg-card/60 backdrop-blur-sm rounded-2xl border border-border/20 p-6 shadow-lg">
                  <h4 className="text-lg font-semibold mb-4 flex items-center gap-2">
                    <span className="w-6 h-6 rounded-full bg-gradient-to-r from-muted/20 to-muted/10 flex items-center justify-center text-xs font-medium">
                      ℹ️
                    </span>
                    Template Information
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                      <span className="text-sm font-medium text-muted-foreground">Created:</span>
                      <p className="text-sm font-medium">{new Date(templateData.createdAt).toLocaleString()}</p>
                    </div>
                    {templateData.updatedAt && (
                      <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20">
                        <span className="text-sm font-medium text-muted-foreground">Last Updated:</span>
                        <p className="text-sm font-medium">{new Date(templateData.updatedAt).toLocaleString()}</p>
                      </div>
                    )}
                    <div className="bg-background/60 backdrop-blur-sm rounded-xl p-4 border border-border/20 md:col-span-2">
                      <span className="text-sm font-medium text-muted-foreground">Physical ID:</span>
                      <p className="text-sm font-mono font-medium">{templateData.physicalId}</p>
                    </div>
                  </div>
                </div>
              </motion.div>
          ) : (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.3 }}
              className="py-20 text-center"
            >
              <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
              </div>
              <p className="text-lg font-medium text-muted-foreground">Loading template data...</p>
            </motion.div>
          )}
          
          {error && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className="bg-destructive/10 border border-destructive/20 rounded-2xl p-6 text-center"
            >
              <div className="w-12 h-12 mx-auto mb-4 rounded-full bg-destructive/20 flex items-center justify-center">
                <span className="text-destructive text-xl">⚠️</span>
              </div>
              <p className="text-destructive font-medium">Error loading template</p>
              <p className="text-destructive/70 text-sm mt-2">{error}</p>
            </motion.div>
          )}
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
