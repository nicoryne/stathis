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
};