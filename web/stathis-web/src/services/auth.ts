'use server';

import { createClient } from '@/lib/supabase/server';
import {
  ForgotPasswordFormValues,
  LoginFormValues,
  RegisterFormValues
} from '@/lib/validations/auth';
import { Provider } from '@supabase/supabase-js';
import { revalidatePath } from 'next/cache';
import { redirect } from 'next/navigation';

export const register = async (form: RegisterFormValues) => {
  const supabase = await createClient();

  const email = form.email;
  const password = form.password;
  const firstName = form.firstName;
  const lastName = form.lastName;
  const role = 'teacher'

  const { error } = await supabase.auth.signUp({
    email,
    password,
    options: {
      data: {
        first_name: firstName,
        last_name: lastName,
        user_role: role,
      }
    }
  });
  
  if (error) {
    throw new Error(error.message);
  }
};

export const loginWithEmail = async (form: LoginFormValues) => {
  const supabase = await createClient();

  const email = form.email;
  const password = form.password;

  const { error } = await supabase.auth.signInWithPassword({email, password});

  if (error) {
    throw new Error(error.message);
  }
};

export const loginWithOAuth = async (provider: Provider) => {
  const supabase = await createClient();

  const authCallbackUrl = `${process.env.NEXT_PUBLIC_SUPABASE_URL}/auth/v1/callback`

  const { data, error } = await supabase.auth.signInWithOAuth({ provider, options: {
    redirectTo: authCallbackUrl
  } })

  if (data.url) {
    redirect(data.url)
  }


  if (error) {
    throw new Error(error.message);
  }
}


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

export const getUserDetails = async () => {
  const supabase = await createClient();

  const { data, error } = await supabase.auth.getUserIdentities();

  if (data) {
    const identity = data.identities[0].identity_data

    return identity;
  }
  
  if (error) {
    throw new Error(error.message);
  }
}
