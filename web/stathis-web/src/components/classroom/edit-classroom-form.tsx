import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useMutation } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { 
  Form, 
  FormControl, 
  FormField, 
  FormItem, 
  FormLabel, 
  FormMessage 
} from '@/components/ui/form';
import { ClassroomResponseDTO, updateClassroom } from '@/services/api-classroom-client';

// Form validation schema
const classroomSchema = z.object({
  name: z.string().min(3, 'Name must be at least 3 characters').max(100),
  description: z.string().min(10, 'Description must be at least 10 characters').max(1000)
});

// Form values type
type ClassroomFormValues = z.infer<typeof classroomSchema>;

interface EditClassroomFormProps {
  classroom: ClassroomResponseDTO;
  onSuccess: () => void;
  onCancel: () => void;
}

export function EditClassroomForm({ classroom, onSuccess, onCancel }: EditClassroomFormProps) {
  const form = useForm<ClassroomFormValues>({
    resolver: zodResolver(classroomSchema),
    defaultValues: {
      name: classroom.name,
      description: classroom.description
    }
  });

  const updateClassroomMutation = useMutation({
    mutationFn: (data: ClassroomFormValues) => 
      updateClassroom(classroom.physicalId, data),
    onSuccess: () => {
      toast.success('Classroom updated successfully');
      onSuccess();
      form.reset();
      
      // Force a page refresh after successful update
      window.location.href = window.location.pathname;
    },
    onError: (error) => {
      toast.error(`Failed to update classroom: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });

  const onSubmit = (values: ClassroomFormValues) => {
    updateClassroomMutation.mutate(values);
  };

  // Create a handler that calls the provided onCancel and refreshes the page
  const handleCancel = () => {
    // Call the provided onCancel function
    onCancel();
    
    // Force a page refresh after a brief delay
    setTimeout(() => {
      window.location.href = window.location.pathname;
    }, 100);
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
                <Input {...field} />
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
                  className="min-h-[100px]"
                  {...field} 
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex justify-end space-x-2 pt-4">
          <Button 
            type="button" 
            variant="outline" 
            onClick={handleCancel}
            disabled={updateClassroomMutation.isPending}
          >
            Cancel
          </Button>
          <Button 
            type="submit"
            disabled={updateClassroomMutation.isPending}
          >
            {updateClassroomMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Saving...
              </>
            ) : 'Save Changes'}
          </Button>
        </div>
      </form>
    </Form>
  );
}
