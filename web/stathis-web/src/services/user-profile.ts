import { createBrowserClient } from '@supabase/ssr';
import {  UserProfileFormValues } from '@/lib/validations/user-profile';

export const getUserProfile = async () => {
  const supabase = createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL || '',
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
  );

  const { data: { user }, error: userError } = await supabase.auth.getUser();

  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }

  const { data: userProfile, error: userProfileError } = await supabase
    .from('user_profiles')
    .select('*')
    .eq('id', user.id)
    .single();

  if (userProfileError) {
    console.error('User profile error:', userProfileError);
    throw new Error('Failed to fetch user profile');
  }

  return userProfile;
};

export const updateUserProfile = async (form: UserProfileFormValues) => {
  const supabase = createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL || '',
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
  );

  const { data: { user }, error: userError } = await supabase.auth.getUser();

  if (userError || !user) {
    console.error('Auth error:', userError);
    throw new Error('Authentication required');
  }

  const id = user.id;
  const email = user.email;
  const first_name = form.first_name;
  const last_name = form.last_name;
  const user_role = form.user_role;
  const picture_url = form.picture_url;
  const school_attending = form.school_attending;
  const year_level = form.year_level;
  const course_enrolled = form.course_enrolled;

  const { data, error } = await supabase
    .from('user_profiles')
    .update({
      first_name,
      last_name,
      email,
      user_role,
      picture_url,
      school_attending,
      year_level,
      course_enrolled,
    })
    .eq('id', id)
    .select();

  if (error) {
    console.error('Supabase error:', error);
    throw new Error(error.message);
  }

  return true;
};