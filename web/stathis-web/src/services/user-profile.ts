import { createBrowserClient } from '@supabase/ssr';
import { UserProfileFormValues } from '@/lib/validations/user-profile';

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

  try {
    // First try to get the user profile from the users table
    const { data: userProfile, error: profileError } = await supabase
      .from('users')
      .select('*')
      .eq('id', user.id)
      .single();

    if (userProfile) {
      return userProfile;
    }

    // If not available in users table, try to get user metadata from auth.users
    const { data: userData } = await supabase.auth.getUser();
    
    if (userData?.user?.user_metadata) {
      return {
        id: user.id,
        email: user.email,
        first_name: userData.user.user_metadata.first_name || '',
        last_name: userData.user.user_metadata.last_name || '',
        user_role: userData.user.user_metadata.user_role || '',
        picture_url: userData.user.user_metadata.picture_url || '',
        school_attending: userData.user.user_metadata.school_attending || '',
        year_level: userData.user.user_metadata.year_level || '',
        course_enrolled: userData.user.user_metadata.course_enrolled || ''
      };
    }

    // Return minimal info if nothing else is available
    return {
      id: user.id,
      email: user.email,
      first_name: '',
      last_name: '',
      user_role: '',
      picture_url: '',
      school_attending: '',
      year_level: '',
      course_enrolled: ''
    };
  } catch (error) {
    console.error('Error fetching user profile:', error);
    
    // Return basic user info from auth
    return {
      id: user.id,
      email: user.email,
      first_name: '',
      last_name: '',
      user_role: '',
      picture_url: '',
      school_attending: '',
      year_level: '',
      course_enrolled: ''
    };
  }
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

  try {
    // Try to update user metadata in auth.users
    const { error: updateError } = await supabase.auth.updateUser({
      data: {
        first_name,
        last_name,
        user_role,
        picture_url,
        school_attending,
        year_level,
        course_enrolled
      }
    });

    if (updateError) {
      console.error('Error updating user metadata:', updateError);
      throw new Error(updateError.message);
    }

    // Try to update or insert into users table if it exists
    try {
      // Check if the user exists in the users table
      const { data: existingUser } = await supabase
        .from('users')
        .select('id')
        .eq('id', id)
        .single();

      if (existingUser) {
        // Update existing user
        const { error: userUpdateError } = await supabase
          .from('users')
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
          .eq('id', id);

        if (userUpdateError) {
          console.error('Error updating user in users table:', userUpdateError);
        }
      } else {
        // Insert new user
        const { error: userInsertError } = await supabase
          .from('users')
          .insert({
            id,
            first_name,
            last_name,
            email,
            user_role,
            picture_url,
            school_attending,
            year_level,
            course_enrolled,
          });

        if (userInsertError) {
          console.error('Error inserting user in users table:', userInsertError);
        }
      }
    } catch (error) {
      console.error('Error with users table operations:', error);
      // We don't throw here since we already updated the auth metadata
    }

    return true;
  } catch (error) {
    console.error('Error in updateUserProfile:', error);
    throw new Error('Failed to update user profile');
  }
};