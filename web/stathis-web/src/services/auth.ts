'use server';

import { createClient } from '@/lib/supabase/server';
import {
  ForgotPasswordFormValues,
  LoginFormValues,
  RegisterFormValues
} from '@/lib/validations/auth';
import { revalidatePath } from 'next/cache';
import { redirect } from 'next/navigation';

export const register = async (form: RegisterFormValues) => {
  const supabase = await createClient();

  const email = form.email;
  const password = form.password;
  const name = form.name;

  const { error } = await supabase.auth.signUp({
    email,
    password,
    options: {
      data: {
        display_name: name
      }
    }
  });
  
  if (error) {
    throw new Error(error.message);
  }
};

export const login = async (form: LoginFormValues) => {
  const supabase = await createClient();

  const email = form.email;
  const password = form.password;

  const { error } = await supabase.auth.signInWithPassword({email, password});

  if (error) {
    throw new Error(error.message);
  }
};

export const logout = async () => {
  const supabase = await createClient();

  const { error } = await supabase.auth.signOut();
  
  if (error) {
    throw new Error(error.message);
  }

  revalidatePath('/')
  redirect('/')
};

export const forgotPassword = async (form: ForgotPasswordFormValues) => {
  const supabase = await createClient();

  const email = form.email;

  const { data, error } = await supabase.auth.resetPasswordForEmail(email);

  return { data, error };
};

export const resendEmailVerification = async (email: string) => {
  const supabase = await createClient();

  const { error } = await supabase.auth.resend({
    type: 'signup',
    email: email
  });

  return { error };
};
