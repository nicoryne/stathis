'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Eye } from 'lucide-react';
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
  
  const fetchTemplate = async () => {
    if (!templateId) {
      toast.error('No template ID provided');
      return;
    }
    
    setIsLoading(true);
    
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
    } catch (error) {
      toast.error('Failed to load template', {
        description: 'Please try again later'
      });
      console.error('Error fetching template:', error);
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
        <DialogContent className="max-w-3xl max-h-[80vh] overflow-auto">
          <DialogHeader>
            <DialogTitle>
              {templateType.charAt(0).toUpperCase() + templateType.slice(1)} Template Review
            </DialogTitle>
            <DialogDescription>
              Reviewing template ID: {templateId}
            </DialogDescription>
          </DialogHeader>
          
          {templateData ? (
            <div className="space-y-4">
              <div className="grid grid-cols-1 gap-2">
                <div>
                  <h3 className="text-lg font-semibold">{templateData.title}</h3>
                  <p className="text-sm text-muted-foreground">{templateData.description || templateData.instruction}</p>
                </div>
                
                {/* Template-specific content rendering */}
                {templateType === 'lesson' && (
                  <div className="border rounded-lg p-4 bg-muted/20">
                    <h4 className="font-medium mb-2">Lesson Content</h4>
                    <div className="prose prose-sm max-w-none">
                      <pre className="text-xs overflow-auto p-2 bg-muted rounded-md">
                        {JSON.stringify(JSON.parse(templateData.content), null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
                
                {templateType === 'quiz' && (
                  <div className="border rounded-lg p-4 bg-muted/20">
                    <h4 className="font-medium mb-2">Quiz Details</h4>
                    <div className="grid grid-cols-2 gap-2 mb-4 text-sm">
                      <div>
                        <span className="font-medium">Max Score:</span> {templateData.maxScore}
                      </div>
                    </div>
                    <h4 className="font-medium mb-2">Questions</h4>
                    <div className="prose prose-sm max-w-none">
                      <pre className="text-xs overflow-auto p-2 bg-muted rounded-md">
                        {JSON.stringify(JSON.parse(templateData.content), null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
                
                {templateType === 'exercise' && (
                  <div className="border rounded-lg p-4 bg-muted/20">
                    <h4 className="font-medium mb-2">Exercise Details</h4>
                    <div className="prose prose-sm max-w-none">
                      <pre className="text-xs overflow-auto p-2 bg-muted rounded-md">
                        {JSON.stringify(JSON.parse(templateData.content), null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
              </div>
              
              <div className="text-xs text-muted-foreground">
                <p>Created: {new Date(templateData.createdAt).toLocaleString()}</p>
                {templateData.updatedAt && (
                  <p>Last Updated: {new Date(templateData.updatedAt).toLocaleString()}</p>
                )}
                <p>Physical ID: {templateData.physicalId}</p>
              </div>
            </div>
          ) : (
            <div className="py-10 text-center">
              <Loader2 className="h-8 w-8 animate-spin mx-auto mb-4" />
              <p>Loading template data...</p>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
