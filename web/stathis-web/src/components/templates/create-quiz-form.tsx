'use client';

import { useState } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Loader2 } from 'lucide-react';
import { createQuizTemplate } from '@/services/templates/api-template-client';
import { QuizTemplateBodyDTO } from '@/services/templates/api-template-client';
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
import {
  Slider
} from '@/components/ui/slider';

// Quiz template form schema
const quizTemplateSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters').max(100, 'Title cannot exceed 100 characters'),
  instruction: z.string().min(3, 'Instructions must be at least 3 characters').max(1000, 'Instructions cannot exceed 1000 characters'),
  maxScore: z.number().min(1, 'Maximum score must be at least 1').max(100, 'Maximum score cannot exceed 100'),
  content: z.string().min(1, 'Content is required')
});

// Form values type
type QuizTemplateFormValues = z.infer<typeof quizTemplateSchema>;

interface CreateQuizFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

export function CreateQuizForm({ onSuccess, onCancel }: CreateQuizFormProps) {
  const form = useForm<QuizTemplateFormValues>({
    resolver: zodResolver(quizTemplateSchema),
    defaultValues: {
      title: '',
      instruction: '',
      maxScore: 10,
      content: '{}'
    }
  });

  const createQuizMutation = useMutation({
    mutationFn: (data: QuizTemplateFormValues) => {
      // Parse the content as JSON
      let parsedContent;
      try {
        parsedContent = JSON.parse(data.content);
      } catch (e) {
        toast.error('Content must be valid JSON');
        throw new Error('Content must be valid JSON');
      }
      
      // Create the template DTO
      const templateData: QuizTemplateBodyDTO = {
        title: data.title,
        instruction: data.instruction,
        maxScore: data.maxScore,
        content: parsedContent
      };
      
      return createQuizTemplate(templateData);
    },
    onSuccess: () => {
      toast.success('Quiz template created successfully');
      onSuccess();
      form.reset();
    },
    onError: (error) => {
      toast.error(`Error creating quiz template: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });

  const onSubmit = (values: QuizTemplateFormValues) => {
    createQuizMutation.mutate(values);
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
                <Input placeholder="Basic Physical Education Quiz" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="instruction"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Instructions</FormLabel>
              <FormControl>
                <Textarea 
                  placeholder="Answer the following questions to the best of your ability..." 
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
          name="maxScore"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Maximum Score: {field.value}</FormLabel>
              <FormControl>
                <Slider
                  min={1}
                  max={100}
                  step={1}
                  defaultValue={[10]}
                  onValueChange={(values) => field.onChange(values[0])}
                  value={[field.value]}
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
                  placeholder='{"questions": [{"question": "What is the primary benefit of regular exercise?", "options": ["Improved mood", "Better sleep", "Increased energy", "All of the above"], "answer": 3}]}' 
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
            disabled={createQuizMutation.isPending}
          >
            {createQuizMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Creating...
              </>
            ) : (
              'Create Quiz Template'
            )}
          </Button>
        </div>
      </form>
    </Form>
  );
}
