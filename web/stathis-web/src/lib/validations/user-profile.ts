import * as z from "zod";

export const userProfileSchema = z.object({
  first_name: z.string().min(1, { message: "First name is required" }),
  last_name: z.string().min(1, { message: "Last name is required" }),
  email: z.string().email({ message: "Invalid email address" }),
  user_role: z.string().min(1, { message: "User role is required" }),
});

export type UserProfileFormValues = z.infer<typeof userProfileSchema>;
