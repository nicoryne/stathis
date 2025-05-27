'use client';

import { useState } from 'react';
import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Loader2 } from 'lucide-react';
import { createClassroom } from '@/services/api-classroom-client';
import { ClassroomBodyDTO } from '@/services/api-classroom';
import { useMutation } from '@tanstack/react-query';
import { getCurrentUserPhysicalId } from '@/lib/utils/jwt';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { DatePicker } from '@/components/ui/date-picker';

// Create classroom form schema
// Note: Schema updated to match the backend ClassroomBodyDTO structure
// This schema matches what our form collects
const classroomSchema = z.object({
  name: z.string().min(3, 'Name must be at least 3 characters').max(100, 'Name cannot exceed 100 characters'),
  description: z.string().min(1, 'Description is required').max(500, 'Description cannot exceed 500 characters')
});

// Form values type (without teacherId which will be added in the API function)
type ClassroomFormValues = z.infer<typeof classroomSchema>;

interface CreateClassroomFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

export function CreateClassroomForm({ onSuccess, onCancel }: CreateClassroomFormProps) {
  const form = useForm<ClassroomFormValues>({
    resolver: zodResolver(classroomSchema),
    defaultValues: {
      name: '',
      description: ''
    }
  });

  const createClassroomMutation = useMutation({
    // Tell TypeScript that createClassroom accepts ClassroomFormValues
    mutationFn: (data: ClassroomFormValues) => createClassroom(data),
    onSuccess: () => {
      onSuccess();
      form.reset();
    }
  });

  const onSubmit = (values: ClassroomFormValues) => {
    // The createClassroom function will add the teacherId for us
    createClassroomMutation.mutate({
      ...values
      // These fields have been removed to match the backend DTO
      // capacity: values.capacity,
      // startDate: values.startDate ? values.startDate.toISOString() : undefined,
      // endDate: values.endDate ? values.endDate.toISOString() : undefined
    });
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Classroom Name</FormLabel>
              <FormControl>
                <Input placeholder="PE Class 101" {...field} />
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
                  placeholder="A brief description of this classroom and its activities" 
                  className="min-h-[100px]"
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
            disabled={createClassroomMutation.isPending}
          >
            {createClassroomMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Creating...
              </>
            ) : (
              'Create Classroom'
            )}
          </Button>
        </div>
      </form>
    </Form>
  );
}
