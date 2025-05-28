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
  FormMessage,
  FormDescription
} from '@/components/ui/form';
import { toast } from 'sonner';
import {
  Slider
} from '@/components/ui/slider';
import { QuizContentBuilder } from './quiz-content-builder';

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
      content: '{"questions":[]}'
    }
  });
  
  // Function to update content from the QuizContentBuilder
  const updateContent = (contentJson: string) => {
    form.setValue('content', contentJson, { shouldValidate: true });
  };

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

  const onSubmit = (values: QuizTemplateFormValues, event?: React.BaseSyntheticEvent) => {
    // Prevent the default form submission behavior which could trigger parent forms
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    createQuizMutation.mutate(values);
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
        className="space-y-4"
      >
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
              <FormLabel>Maximum Score</FormLabel>
              <FormControl>
                <Input
                  type="number"
                  min="1"
                  step="1"
                  placeholder="Enter max score (min: 1)"
                  {...field}
                  onChange={(e) => {
                    // Ensure the value is a positive number
                    const value = parseInt(e.target.value);
                    if (isNaN(value) || value < 1) {
                      field.onChange(1);
                    } else {
                      field.onChange(value);
                    }
                  }}
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
              <FormLabel>Quiz Questions</FormLabel>
              <FormControl>
                <div className="border rounded-md p-4 bg-background">
                  <QuizContentBuilder 
                    initialValue={field.value}
                    onChange={updateContent}
                  />
                </div>
              </FormControl>
              <FormDescription>
                Add multiple-choice questions with options. Select the radio button next to the correct answer for each question.
              </FormDescription>
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
