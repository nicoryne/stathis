import { createClient } from "@/lib/supabase/server";
import { getUserDetails } from "./auth";
import { v4 as uuidv4 } from "uuid";
import { ClassroomFormValues } from "@/lib/validations/classroom";

export const createClassroom = async (form: ClassroomFormValues) => {
  const supabase = await createClient();

  const user = await getUserDetails();
  const name = form.name;
  const description = form.description;

  const { error } = await supabase.from('classrooms').insert({
    id: uuidv4(),
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
    name: name,
    description: description,
    is_active: true,
    teacher_id: user?.id,
  });
  
  if (error) {
    throw new Error(error.message);
  }
};
