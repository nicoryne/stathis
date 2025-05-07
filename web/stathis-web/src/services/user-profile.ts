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
    // First try to get the user profile from the user_profile table
    const { data: userProfile, error: profileError } = await supabase
      .from('user_profile')
      .select('*')
      .eq('user_id', user.id)
      .single();

    if (userProfile) {
      return userProfile;
    }

    // If not available in user_profile table, try to get user metadata from auth.users
    const { data: userData } = await supabase.auth.getUser();
    
    if (userData?.user?.user_metadata) {
      return {
        user_id: user.id,
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
      user_id: user.id,
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
      user_id: user.id,
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

  try {
    const { data: { user }, error: userError } = await supabase.auth.getUser();

    if (userError || !user) {
      console.error('Auth error:', userError);
      throw new Error('Authentication required');
    }

    const user_id = user.id;
    const email = user.email;
    const first_name = form.first_name;
    const last_name = form.last_name;
    const user_role = form.user_role || 'student'; // Default to student if not set
    const picture_url = form.picture_url || null;
    const school_attending = form.school_attending || null;
    const year_level = form.year_level ? parseInt(form.year_level) : null; // Convert to integer or null
    const course_enrolled = form.course_enrolled || null;

    console.log('Updating user with data:', { 
      first_name, last_name, email, user_role, 
      picture_url, school_attending, year_level, course_enrolled 
    });

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
      throw new Error(`Auth update failed: ${updateError.message}`);
    }

    // Try to update or insert into user_profile table if it exists
    try {
      console.log('Checking if user profile exists in user_profile table...');
      
      // Check if the user exists in the user_profile table
      const { data: existingUser, error: checkError } = await supabase
        .from('user_profile')
        .select('user_id')
        .eq('user_id', user_id)
        .single();
        
      if (checkError) {
        if (checkError.code === 'PGRST116') {
          console.log('User not found in user_profile table. Will create new record.');
        } else {
          console.error('Error checking user existence:', checkError);
        }
      } else {
        console.log('User found in user_profile table:', existingUser);
      }

      // Prepare profile data 
      const profileData = {
        first_name,
        last_name,
        email,
        user_role,
        picture_url, 
        school_attending,
        year_level,
        course_enrolled
      };
      
      console.log('Profile data for user_profile table:', JSON.stringify(profileData, null, 2));
      
      // Direct SQL approach for debugging - log the query that would be executed
      console.log(`User profile data to save: 
        - user_id: ${user_id}
        - email: ${email}
        - first_name: ${first_name}
        - last_name: ${last_name}
        - user_role: ${user_role}
        - picture_url: ${picture_url}
        - school_attending: ${school_attending || 'null'}
        - year_level: ${year_level || 'null'}
        - course_enrolled: ${course_enrolled || 'null'}
      `);
      
      if (existingUser) {
        // Update existing user
        console.log(`Updating existing user ${user_id} in user_profile table`);
        const { data: updateData, error: userUpdateError } = await supabase
          .from('user_profile')
          .update(profileData)
          .eq('user_id', user_id)
          .select();

        if (userUpdateError) {
          console.error('Error updating user in user_profile table:', userUpdateError);
          // Don't throw since we already updated auth metadata
        } else {
          console.log('Successfully updated user_profile:', updateData);
        }
      } else {
        // Insert new user
        console.log(`Inserting new user ${user_id} into user_profile table`);
        const { data: insertData, error: userInsertError } = await supabase
          .from('user_profile')
          .insert({
            user_id,
            ...profileData
          })
          .select();

        if (userInsertError) {
          console.error('Error inserting user in user_profile table:', userInsertError);
          // Don't throw since we already updated auth metadata
        } else {
          console.log('Successfully inserted user_profile:', insertData);
        }
      }
    } catch (error) {
      console.error('Error with user_profile table operations:', error);
      // We don't throw here since we already updated the auth metadata
    }

    return true;
  } catch (error) {
    console.error('Error in updateUserProfile:', error);
    throw error; // Rethrow so the UI can handle it
  }
};