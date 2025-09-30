'use client';

import { useState } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Loader2 } from 'lucide-react';
import { createLessonTemplate } from '@/services/templates/api-template-client';
import { LessonTemplateBodyDTO } from '@/services/templates/api-template-client';
import { useMutation } from '@tanstack/react-query';
import { LessonContentBuilder } from './lesson-content-builder';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { toast } from 'sonner';

// Lesson template form schema
const lessonTemplateSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters').max(100, 'Title cannot exceed 100 characters'),
  description: z.string().min(3, 'Description must be at least 3 characters').max(1000, 'Description cannot exceed 1000 characters'),
  content: z.string().min(1, 'Content is required')
});

// Form values type
type LessonTemplateFormValues = z.infer<typeof lessonTemplateSchema>;

interface CreateLessonFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

export function CreateLessonForm({ onSuccess, onCancel }: CreateLessonFormProps) {
  // State to track JSON content from the builder
  const [jsonContent, setJsonContent] = useState<string>('{}');
  const form = useForm<LessonTemplateFormValues>({
    resolver: zodResolver(lessonTemplateSchema),
    defaultValues: {
      title: '',
      description: '',
      content: jsonContent
    }
  });
  
  // Update form when JSON content changes from the builder
  const updateContent = (newJsonContent: string) => {
    setJsonContent(newJsonContent);
    form.setValue('content', newJsonContent);
  };

  const createLessonMutation = useMutation({
    mutationFn: (data: LessonTemplateFormValues) => {
      // Parse the content as JSON
      let parsedContent;
      try {
        parsedContent = JSON.parse(data.content);
      } catch (e) {
        toast.error('Content must be valid JSON');
        throw new Error('Content must be valid JSON');
      }
      
      // Create the template DTO
      const templateData: LessonTemplateBodyDTO = {
        title: data.title,
        description: data.description,
        content: parsedContent
      };
      
      return createLessonTemplate(templateData);
    },
    onSuccess: () => {
      toast.success('Lesson template created successfully');
      onSuccess();
      form.reset();
    },
    onError: (error) => {
      toast.error(`Error creating lesson template: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });

  const onSubmit = (values: LessonTemplateFormValues, event?: React.BaseSyntheticEvent) => {
    // Prevent the default form submission behavior which could trigger parent forms
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    createLessonMutation.mutate(values);
  };

  return (
    <Form {...form}>
      <form 
        onSubmit={(e) => {
          // Explicitly prevent default and stop propagation to avoid triggering parent forms
          e.preventDefault();
          e.stopPropagation();
          form.handleSubmit(onSubmit)(e);
        }} 
        className="flex flex-col h-full max-h-[60vh] overflow-hidden"
      >
        <div className="flex-1 overflow-y-auto pr-4 space-y-4">
        <FormField
          control={form.control}
          name="title"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Title</FormLabel>
              <FormControl>
                <Input placeholder="Introduction to Physical Education" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="description"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Description</FormLabel>
              <FormControl>
                <Textarea 
                  placeholder="A brief description of this lesson" 
                  className="min-h-[100px]"
                  {...field} 
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="content"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Lesson Content</FormLabel>
              <FormControl>
                <div className="border rounded-md p-4 bg-background">
                  <LessonContentBuilder 
                    initialValue={field.value}
                    onChange={updateContent}
                  />
                </div>
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        </div>
        <div className="flex justify-end gap-2 pt-4 sticky bottom-0 bg-background border-t mt-4">
          <Button type="button" variant="outline" onClick={onCancel}>
            Cancel
          </Button>
          <Button 
            type="submit" 
            disabled={createLessonMutation.isPending}
          >
            {createLessonMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Creating...
              </>
            ) : (
              'Create Lesson Template'
            )}
          </Button>
        </div>
      </form>
    </Form>
  );
}
