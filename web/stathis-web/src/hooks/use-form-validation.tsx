'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useForm, UseFormProps, UseFormReturn } from 'react-hook-form';
import * as z from 'zod';

/**
 * Custom hook for form validation using Zod schemas
 * @param schema - The Zod schema to validate against
 * @param defaultValues - Optional default values for the form
 * @returns A fully configured form instance with validation
 */
export function useFormValidation<TSchema extends z.ZodType<any, any, any>>(
  schema: TSchema,
  defaultValues?: z.infer<TSchema> | undefined
): UseFormReturn<z.infer<TSchema>> {
  type FormValues = z.infer<TSchema>;
  
  const formConfig: UseFormProps<FormValues> = {
    resolver: zodResolver(schema),
    defaultValues: defaultValues as UseFormProps<FormValues>['defaultValues'],
    mode: 'onBlur', // Validate on blur for better UX
  };

  return useForm<FormValues>(formConfig);
}
