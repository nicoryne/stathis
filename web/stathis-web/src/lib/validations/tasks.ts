import * as z from "zod";

export const taskSchema = z.object({
  name: z.string().min(1, "Task name is required"),
  description: z.string().optional(),
  type: z.string().min(1, "Task type is required"),
  submission_date: z.string().optional(),
  closing_date: z.string().optional(),
  image_url: z.string().optional(),
  classroom_id: z.string().uuid("Invalid classroom ID"),
  task_template_id: z.string().uuid("Invalid template ID").optional(),
});

export type TaskFormValues = z.infer<typeof taskSchema>;

export const taskFilterSchema = z.object({
  classroomId: z.string().uuid("Invalid classroom ID").optional(),
});

export type TaskFilterValues = z.infer<typeof taskFilterSchema>; 