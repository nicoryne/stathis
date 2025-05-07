'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createClassroom } from '@/services/classroom';
import { classroomSchema, ClassroomFormValues } from '@/lib/validations/classroom';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { toast } from 'sonner';
import { Loader2, CheckCircle2 } from 'lucide-react';
import { useRouter } from 'next/navigation';

interface ClassroomModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  trigger?: React.ReactNode;
}

export function ClassroomModal({ open, onOpenChange, trigger }: ClassroomModalProps) {
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const form = useForm<ClassroomFormValues>({
    resolver: zodResolver(classroomSchema),
    defaultValues: {
      name: '',
      description: '',
    },
  });
  
  // Handle dialog open/close - reset the form state when closed
  const handleOpenChange = (newOpen: boolean) => {
    // If closing the dialog
    if (!newOpen && isLoading) {
      // Reset loading state if dialog is closed while loading
      setIsLoading(false);
    }
    
    // Reset form when dialog closes
    if (!newOpen) {
      form.reset();
    }
    
    // Propagate to parent
    onOpenChange(newOpen);
  };

  const onSubmit = async (values: ClassroomFormValues) => {
    try {
      setIsLoading(true);
      await createClassroom(values);
      
      // Reset form
      form.reset();
      
      // Show success toast with modern design
      toast.success(
        <div className="flex items-center gap-3">
          <CheckCircle2 className="h-5 w-5 text-primary" />
          <div>
            <p className="font-medium">Classroom created</p>
            <p className="text-sm text-muted-foreground">
              {values.name} has been successfully created
            </p>
          </div>
        </div>,
        {
          duration: 4000,
          className: "bg-background border-primary/20"
        }
      );
      
      // Reset loading state
      setIsLoading(false);
      
      // Close the modal
      onOpenChange(false);
      
      // Add success message to URL for dashboard banner and refresh in one operation
      const successParams = new URLSearchParams();
      successParams.set('success', 'true');
      successParams.set('message', `Classroom "${values.name}" has been successfully created.`);
      
      // Update URL with params and refresh the page to show new classroom
      router.push(`?${successParams.toString()}`);
      
    } catch (error) {
      // Show error toast
      toast.error(
        <div>
          <p className="font-medium">Failed to create classroom</p>
          <p className="text-sm">
            {error instanceof Error ? error.message : 'An unexpected error occurred'}
          </p>
        </div>,
        {
          duration: 5000,
        }
      );
      
      // Reset loading state
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      {trigger && <DialogTrigger asChild>{trigger}</DialogTrigger>}
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Create New Classroom</DialogTitle>
          <DialogDescription>
            Add details to create a new classroom for your students.
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Classroom Name</FormLabel>
                  <FormControl>
                    <Input placeholder="Enter classroom name" {...field} />
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
                      placeholder="Enter a description for this classroom"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button type="submit" disabled={isLoading} className="relative">
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Creating...
                  </>
                ) : (
                  'Create Classroom'
                )}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
