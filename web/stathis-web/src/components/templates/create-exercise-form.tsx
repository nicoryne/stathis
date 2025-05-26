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
  exerciseType: z.string().uuid('Exercise type must be a valid UUID'),
  exerciseDifficulty: z.string().uuid('Exercise difficulty must be a valid UUID'),
  goalReps: z.string().uuid('Goal reps must be a valid UUID'),
  goalAccuracy: z.string().uuid('Goal accuracy must be a valid UUID'),
  goalTime: z.string().uuid('Goal time must be a valid UUID')
});

// Form values type
type ExerciseTemplateFormValues = z.infer<typeof exerciseTemplateSchema>;

interface CreateExerciseFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

// Mock data for dropdowns (in a real app, these would come from API calls)
const exerciseTypes = [
  { id: "12345678-1234-1234-1234-123456789012", name: "PUSH_UP" },
  { id: "12345678-1234-1234-1234-123456789013", name: "SQUATS" }
];

const exerciseDifficulties = [
  { id: "12345678-1234-1234-1234-123456789014", name: "BEGINNER" },
  { id: "12345678-1234-1234-1234-123456789015", name: "EXPERT" }
];

const goalOptions = [
  { id: "12345678-1234-1234-1234-123456789016", name: "10", type: "reps" },
  { id: "12345678-1234-1234-1234-123456789017", name: "20", type: "reps" },
  { id: "12345678-1234-1234-1234-123456789018", name: "30", type: "reps" },
  { id: "12345678-1234-1234-1234-123456789019", name: "70%", type: "accuracy" },
  { id: "12345678-1234-1234-1234-123456789020", name: "80%", type: "accuracy" },
  { id: "12345678-1234-1234-1234-123456789021", name: "90%", type: "accuracy" },
  { id: "12345678-1234-1234-1234-123456789022", name: "30 seconds", type: "time" },
  { id: "12345678-1234-1234-1234-123456789023", name: "1 minute", type: "time" },
  { id: "12345678-1234-1234-1234-123456789024", name: "2 minutes", type: "time" }
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
      // Create the template DTO
      const templateData: ExerciseTemplateBodyDTO = {
        title: data.title,
        description: data.description,
        exerciseType: data.exerciseType,
        exerciseDifficulty: data.exerciseDifficulty,
        goalReps: data.goalReps,
        goalAccuracy: data.goalAccuracy,
        goalTime: data.goalTime
      };
      
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

  const onSubmit = (values: ExerciseTemplateFormValues) => {
    createExerciseMutation.mutate(values);
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
                      <SelectItem key={type.id} value={type.id}>
                        {type.name}
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
                      <SelectItem key={difficulty.id} value={difficulty.id}>
                        {difficulty.name}
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
                  {goalOptions.filter(goal => goal.type === 'reps').map((goal) => (
                    <SelectItem key={goal.id} value={goal.id}>
                      {goal.name}
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
                  {goalOptions.filter(goal => goal.type === 'accuracy').map((goal) => (
                    <SelectItem key={goal.id} value={goal.id}>
                      {goal.name}
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
                  {goalOptions.filter(goal => goal.type === 'time').map((goal) => (
                    <SelectItem key={goal.id} value={goal.id}>
                      {goal.name}
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
