import * as z from "zod";

export const userProfileSchema = z.object({
  first_name: z.string().min(1, { message: "First name is required" }),
  last_name: z.string().min(1, { message: "Last name is required" }),
  email: z.string().email({ message: "Invalid email address" }),
  user_role: z.string().default("student").optional(),

  // Optional fields
  picture_url: z.string().optional().nullable(),
  school_attending: z.string().optional().nullable(),
  year_level: z.string().optional().nullable(),
  course_enrolled: z.string().optional().nullable(),
});

export type UserProfileFormValues = z.infer<typeof userProfileSchema>;
