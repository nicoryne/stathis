import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useMutation } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Loader2, Save, Edit } from 'lucide-react';
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
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <FormField
            control={form.control}
            name="name"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="text-base font-semibold">Classroom Name</FormLabel>
                <FormControl>
                  <Input 
                    {...field} 
                    className="h-12 rounded-xl border-border/50 bg-background/50 backdrop-blur-sm"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.1 }}
        >
          <FormField
            control={form.control}
            name="description"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="text-base font-semibold">Description</FormLabel>
                <FormControl>
                  <Textarea 
                    className="min-h-[120px] rounded-xl border-border/50 bg-background/50 backdrop-blur-sm"
                    {...field} 
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="flex justify-end space-x-3 pt-4"
        >
          <Button 
            type="button" 
            variant="outline" 
            onClick={handleCancel}
            disabled={updateClassroomMutation.isPending}
            className="bg-card/80 backdrop-blur-xl border-border/50 hover:bg-card/90"
          >
            Cancel
          </Button>
          <Button 
            type="submit"
            disabled={updateClassroomMutation.isPending}
            className="bg-gradient-to-r from-secondary to-accent hover:from-secondary/90 hover:to-accent/90 shadow-lg hover:shadow-xl transition-all duration-200"
          >
            {updateClassroomMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Saving...
              </>
            ) : (
              <>
                <Save className="mr-2 h-4 w-4" />
                Save Changes
              </>
            )}
          </Button>
        </motion.div>
      </form>
    </Form>
  );
}
