'use client';

import { useState } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Loader2 } from 'lucide-react';
import { createExerciseTemplate } from '@/services/templates/api-template-client';
import { ExerciseTemplateBodyDTO } from '@/services/templates/api-template-client';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { toast } from 'sonner';

// Exercise template form schema
const exerciseTemplateSchema = z.object({
  title: z.string().min(3, 'Title must be at least 3 characters').max(100, 'Title cannot exceed 100 characters'),
  description: z.string().min(3, 'Description must be at least 3 characters').max(500, 'Description cannot exceed 500 characters'),
  exerciseType: z.string().min(1, 'Exercise type is required'),
  exerciseDifficulty: z.string().min(1, 'Exercise difficulty is required'),
  goalReps: z.string().min(1, 'Goal reps is required').regex(/^[0-9]+$/, 'Goal reps must be a number'),
  goalAccuracy: z.string().min(1, 'Goal accuracy is required').regex(/^[0-9]+$/, 'Goal accuracy must be a number'),
  goalTime: z.string().min(1, 'Goal time is required').regex(/^[0-9]+$/, 'Goal time must be a number')
});

// Form values type
type ExerciseTemplateFormValues = z.infer<typeof exerciseTemplateSchema>;

interface CreateExerciseFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

// Options based on API requirements
const exerciseTypes = [
  { value: "PUSH_UP", label: "Push Up" },
  { value: "SQUATS", label: "Squats" }
];

const exerciseDifficulties = [
  { value: "BEGINNER", label: "Beginner" },
  { value: "EXPERT", label: "Expert" }
];

const goalRepsOptions = [
  { value: "10", label: "10 repetitions" },
  { value: "20", label: "20 repetitions" },
  { value: "30", label: "30 repetitions" }
];

const goalAccuracyOptions = [
  { value: "70", label: "70%" },
  { value: "80", label: "80%" },
  { value: "90", label: "90%" }
];

const goalTimeOptions = [
  { value: "30", label: "30 seconds" },
  { value: "60", label: "1 minute" },
  { value: "120", label: "2 minutes" }
];

export function CreateExerciseForm({ onSuccess, onCancel }: CreateExerciseFormProps) {
  const form = useForm<ExerciseTemplateFormValues>({
    resolver: zodResolver(exerciseTemplateSchema),
    defaultValues: {
      title: '',
      description: '',
      exerciseType: '',
      exerciseDifficulty: '',
      goalReps: '',
      goalAccuracy: '',
      goalTime: ''
    }
  });

  const createExerciseMutation = useMutation({
    mutationFn: (data: ExerciseTemplateFormValues) => {
      // Create the template DTO with proper format for API
      const templateData: ExerciseTemplateBodyDTO = {
        title: data.title,
        description: data.description,
        exerciseType: data.exerciseType as 'PUSH_UP' | 'SQUATS',
        exerciseDifficulty: data.exerciseDifficulty as 'BEGINNER' | 'EXPERT',
        goalReps: data.goalReps,
        goalAccuracy: data.goalAccuracy,
        goalTime: data.goalTime
      };
      
      console.log('Sending exercise template data:', templateData);
      
      return createExerciseTemplate(templateData);
    },
    onSuccess: () => {
      toast.success('Exercise template created successfully');
      onSuccess();
      form.reset();
    },
    onError: (error) => {
      toast.error(`Error creating exercise template: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });

  const onSubmit = (values: ExerciseTemplateFormValues, event?: React.BaseSyntheticEvent) => {
    // Prevent the default form submission behavior which could trigger parent forms
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    createExerciseMutation.mutate(values);
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
                <Input placeholder="Basic Push-Up Exercise" {...field} />
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
                  placeholder="A set of push-ups to build upper body strength..." 
                  className="min-h-[100px]"
                  {...field} 
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="exerciseType"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Exercise Type</FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select exercise type" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {exerciseTypes.map((type) => (
                      <SelectItem key={type.value} value={type.value}>
                        {type.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="exerciseDifficulty"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Difficulty Level</FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select difficulty" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {exerciseDifficulties.map((difficulty) => (
                      <SelectItem key={difficulty.value} value={difficulty.value}>
                        {difficulty.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="goalReps"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Goal Repetitions</FormLabel>
              <Select onValueChange={field.onChange} defaultValue={field.value}>
                <FormControl>
                  <SelectTrigger>
                    <SelectValue placeholder="Select target repetitions" />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  {goalRepsOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FormDescription>
                Target number of repetitions for this exercise
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="goalAccuracy"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Goal Accuracy</FormLabel>
              <Select onValueChange={field.onChange} defaultValue={field.value}>
                <FormControl>
                  <SelectTrigger>
                    <SelectValue placeholder="Select target accuracy" />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  {goalAccuracyOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FormDescription>
                Target accuracy percentage for exercise form
              </FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="goalTime"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Goal Time</FormLabel>
              <Select onValueChange={field.onChange} defaultValue={field.value}>
                <FormControl>
                  <SelectTrigger>
                    <SelectValue placeholder="Select target time" />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  {goalTimeOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FormDescription>
                Target time to complete the exercise
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
            disabled={createExerciseMutation.isPending}
          >
            {createExerciseMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Creating...
              </>
            ) : (
              'Create Exercise Template'
            )}
          </Button>
        </div>
      </form>
    </Form>
  );
}
