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
  const form = useForm<LessonTemplateFormValues>({
    resolver: zodResolver(lessonTemplateSchema),
    defaultValues: {
      title: '',
      description: '',
      content: '{}'
    }
  });

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

  const onSubmit = (values: LessonTemplateFormValues) => {
    createLessonMutation.mutate(values);
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
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
              <FormLabel>Content (JSON format)</FormLabel>
              <FormControl>
                <Textarea 
                  placeholder='{"sections": [{"title": "Introduction", "content": "Welcome to the lesson..."}]}' 
                  className="min-h-[200px] font-mono text-sm"
                  {...field} 
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex justify-end gap-2 pt-4">
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
