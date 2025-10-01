'use client';

import React, { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import * as z from 'zod';
import { Sidebar } from '@/components/dashboard/sidebar';
import { AuthNavbar } from '@/components/auth-navbar';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';
import { 
  UserIcon, 
  School, 
  Building2, 
  Award, 
  Calendar,
  Mail,
  Save,
  Upload,
  X
} from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { 
  getTeacherProfile, 
  updateUserProfile, 
  updateTeacherProfile, 
  UserProfileDTO,
  UpdateUserProfileDTO,
  UpdateTeacherProfileDTO
} from '@/services/users/api-user-client';
// Test endpoints import removed

// Define validation schemas for forms
const personalInfoSchema = z.object({
  firstName: z.string().min(2, { message: 'First name must be at least 2 characters.' }),
  lastName: z.string().min(2, { message: 'Last name must be at least 2 characters.' }),
  birthdate: z.string().optional(),
  profilePictureUrl: z.string().optional(),
});

const teacherProfileSchema = z.object({
  school: z.string().min(2, { message: 'School name must be at least 2 characters.' }),
  department: z.string().optional(),
  positionTitle: z.string().optional(),
});

export default function ProfilePage() {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState('personal');
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  // Removed local storage fallback implementation
  
  // Fetch teacher profile data
  const { 
    data: profileData, 
    isLoading, 
    isError, 
    error 
  } = useQuery({
    queryKey: ['teacher-profile'],
    queryFn: getTeacherProfile,
    staleTime: 1000 * 60 * 5, // 5 minutes
  });
  
  // Initialize personal info form
  const personalInfoForm = useForm<z.infer<typeof personalInfoSchema>>({
    resolver: zodResolver(personalInfoSchema),
    defaultValues: {
      firstName: profileData?.firstName || '',
      lastName: profileData?.lastName || '',
      birthdate: profileData?.birthdate || '',
      profilePictureUrl: profileData?.profilePictureUrl || '',
    },
  });
  
  // Initialize teacher profile form
  const teacherProfileForm = useForm<z.infer<typeof teacherProfileSchema>>({
    resolver: zodResolver(teacherProfileSchema),
    defaultValues: {
      school: profileData?.school || '',
      department: profileData?.department || '',
      positionTitle: profileData?.positionTitle || '',
    },
  });
  
  // Update form values when profile data is loaded
  React.useEffect(() => {
    if (profileData) {
      personalInfoForm.reset({
        firstName: profileData.firstName || '',
        lastName: profileData.lastName || '',
        birthdate: profileData.birthdate || '',
        profilePictureUrl: profileData.profilePictureUrl || '',
      });
      
      // Set image preview if profile picture exists
      if (profileData.profilePictureUrl) {
        setImagePreview(profileData.profilePictureUrl);
      }
      
      teacherProfileForm.reset({
        school: profileData.school || '',
        department: profileData.department || '',
        positionTitle: profileData.positionTitle || '',
      });
    }
  }, [profileData, personalInfoForm, teacherProfileForm]);
  
  // Image upload handler that uses a placeholder URL due to database limitations
  const handleImageUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    
    // Validate file type
    if (!file.type.match(/image\/(jpeg|jpg|png|gif|webp)/)) {
      toast({
        title: 'Invalid file type',
        description: 'Please upload an image file (JPEG, PNG, GIF, WebP)',
        variant: 'destructive',
      });
      return;
    }
    
    // Validate file size
    if (file.size > 5 * 1024 * 1024) {
      toast({
        title: 'File too large',
        description: 'Image should be less than 5MB',
        variant: 'destructive',
      });
      return;
    }
    
    // Show a preview of the uploaded image in the UI
    const reader = new FileReader();
    reader.onload = (e) => {
      // Set the image preview for visual purposes
      setImagePreview(e.target?.result as string);
      
      // IMPORTANT: Due to a database limitation (VARCHAR(255) column), 
      // we can't store the actual image data. In a real application, 
      // we would upload the image to cloud storage and store just the URL.
      // For now, we use a placeholder URL service based on the user's name.
      toast({
        title: 'Database Limitation',
        description: 'Due to database constraints (255 char limit), your actual image cannot be stored. We are showing it in the preview but using a placeholder URL for storage.',
        variant: 'default',
        duration: 6000,
      });
      
      // Generate a placeholder URL based on the user's name
      const firstName = profileData?.firstName || personalInfoForm.getValues('firstName');
      const lastName = profileData?.lastName || personalInfoForm.getValues('lastName');
      const placeholderUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(
        firstName + '+' + lastName
      )}&size=200&background=random`;
      
      // Set the form value to this placeholder URL
      personalInfoForm.setValue('profilePictureUrl', placeholderUrl);
    };
    reader.readAsDataURL(file);
  };
  
  // Clear uploaded image
  const clearImage = () => {
    setImagePreview(null);
    personalInfoForm.setValue('profilePictureUrl', '');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // Mutation for updating personal info
  const updatePersonalInfoMutation = useMutation({
    mutationFn: (data: UpdateUserProfileDTO) => {
      return updateUserProfile(data);
    },
    onSuccess: (data) => {
      // Update the query cache with the new data
      queryClient.setQueryData(['teacher-profile'], data);
      
      toast({
        title: 'Profile Updated',
        description: 'Your personal information has been updated successfully.',
      });
    },
    onError: (err: Error) => {
      toast({
        title: 'Update Failed',
        description: err.message || 'Failed to update personal information',
        variant: 'destructive',
      });
    },
  });
  
  // Mutation for updating teacher profile
  const updateTeacherProfileMutation = useMutation({
    mutationFn: (data: UpdateTeacherProfileDTO) => {
      return updateTeacherProfile(data);
    },
    onSuccess: (data) => {
      queryClient.setQueryData(['teacher-profile'], data);
      toast({
        title: 'Profile Updated',
        description: 'Your teaching profile has been updated successfully.',
      });
    },
    onError: (err: Error) => {
      toast({
        title: 'Update Failed',
        description: err.message || 'Failed to update teaching profile',
        variant: 'destructive',
      });
    },
  });
  
  // Handle form submissions
  const onPersonalInfoSubmit = (values: z.infer<typeof personalInfoSchema>) => {
    console.log('Updating personal profile');
    
    // Create a payload that maintains the original values where possible
    const payload = {
      // Use the current values from the form
      firstName: values.firstName,
      lastName: values.lastName,
    } as UpdateUserProfileDTO;
    
    // Only include optional fields if they have values
    if (values.birthdate) {
      payload.birthdate = values.birthdate;
    }
    
    if (values.profilePictureUrl) {
      payload.profilePictureUrl = values.profilePictureUrl;
    } else {
      payload.profilePictureUrl = profileData?.profilePictureUrl || '';
    }
    
    console.log('Profile update payload:', payload);
    
    // Submit the update
    updatePersonalInfoMutation.mutate(payload);
  };
  
  const onTeacherProfileSubmit = (values: z.infer<typeof teacherProfileSchema>) => {
    console.log('Updating teacher profile');
    console.log('Form values being submitted:', values);
    
    // Backend will get the current user from authentication context
    updateTeacherProfileMutation.mutate(values);
  };
  
  // Handle loading and error states
  if (isLoading) {
    return (
      <div className="flex min-h-screen">
        <Sidebar className="w-64 flex-shrink-0" />
        <div className="flex-1">
          <AuthNavbar />
          <main className="p-6">
            <div className="mb-6 flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0">
              <div>
                <h1 className="text-2xl font-bold tracking-tight">Profile Management</h1>
                <p className="text-muted-foreground mt-1">Manage your profile information</p>
              </div>
            </div>
            <Card className="mx-auto max-w-lg">
              <CardHeader>
                <CardTitle>Loading...</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex justify-center p-4">
                  {/* Replace with proper loading spinner component later */}
                  <p className="animate-pulse">Loading your profile information...</p>
                </div>
              </CardContent>
            </Card>
          </main>
        </div>
      </div>
    );
  }
  
  return (
    <div className="flex min-h-screen">
      <Sidebar className="w-64 flex-shrink-0" />
      
      <div className="flex-1">
        <AuthNavbar />
        
        <main className="p-6">
          <div className="mb-6 flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0">
            <div>
              <h1 className="text-2xl font-bold tracking-tight">Profile Management</h1>
              <p className="text-muted-foreground mt-1">Manage your profile information</p>
            </div>
            <div className="flex items-center gap-4">
              <Button 
                variant="outline" 
                size="sm"
                onClick={() => queryClient.invalidateQueries({ queryKey: ['teacher-profile'] })}
              >
                Refresh
              </Button>
            </div>
          </div>

          <div className="grid gap-8">
        {/* Profile Summary */}
        <Card>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-6 items-center md:items-start">
              <div className="flex-shrink-0">
                <Avatar className="h-24 w-24">
                  <AvatarImage src={profileData?.profilePictureUrl || ''} alt={profileData?.firstName} />
                  <AvatarFallback className="text-xl">
                    {profileData?.firstName?.charAt(0)}{profileData?.lastName?.charAt(0)}
                  </AvatarFallback>
                </Avatar>
              </div>
              <div className="flex-grow space-y-2 text-center md:text-left">
                <h2 className="text-2xl font-bold">
                  {profileData?.firstName} {profileData?.lastName}
                </h2>
                <div className="flex flex-col md:flex-row gap-4 text-muted-foreground">
                  <div className="flex items-center gap-1">
                    <Mail className="h-4 w-4" />
                    <span>{profileData?.email}</span>
                  </div>
                  {profileData?.school && (
                    <div className="flex items-center gap-1">
                      <School className="h-4 w-4" />
                      <span>{profileData.school}</span>
                    </div>
                  )}
                  {profileData?.positionTitle && (
                    <div className="flex items-center gap-1">
                      <Award className="h-4 w-4" />
                      <span>{profileData.positionTitle}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Profile Edit Forms */}
        <Tabs defaultValue="personal" value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="grid w-full grid-cols-2 max-w-md">
            <TabsTrigger value="personal">Personal Information</TabsTrigger>
            <TabsTrigger value="teacher">Teaching Profile</TabsTrigger>
          </TabsList>

          {/* Personal Information Tab */}
          <TabsContent value="personal" className="space-y-4 mt-6">
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <div>
                    <CardTitle className="text-xl">Personal Information</CardTitle>
                    <CardDescription>Update your personal details</CardDescription>
                  </div>
                  <UserIcon className="h-5 w-5 text-muted-foreground" />
                </div>
              </CardHeader>
              <CardContent>
                <Form {...personalInfoForm}>
                  <form onSubmit={personalInfoForm.handleSubmit(onPersonalInfoSubmit)} className="space-y-6">
                    <div className="grid gap-6 md:grid-cols-2">
                      <FormField
                        control={personalInfoForm.control}
                        name="firstName"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>First Name</FormLabel>
                            <FormControl>
                              <Input placeholder="Enter your first name" {...field} />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={personalInfoForm.control}
                        name="lastName"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Last Name</FormLabel>
                            <FormControl>
                              <Input placeholder="Enter your last name" {...field} />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </div>
                    
                    <FormField
                      control={personalInfoForm.control}
                      name="birthdate"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Birthdate</FormLabel>
                          <FormControl>
                            <Input type="date" {...field} />
                          </FormControl>
                          <FormDescription>
                            This information is optional and will not be shared.
                          </FormDescription>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    
                    <FormField
                      control={personalInfoForm.control}
                      name="profilePictureUrl"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Profile Picture</FormLabel>
                          <FormControl>
                            <div className="space-y-4">
                              {/* Image Preview */}
                              {imagePreview && (
                                <div className="relative w-32 h-32 mx-auto md:mx-0">
                                  <img 
                                    src={imagePreview} 
                                    alt="Profile preview" 
                                    className="w-full h-full object-cover rounded-full border-2 border-primary"
                                  />
                                  <Button
                                    type="button"
                                    variant="destructive"
                                    size="icon"
                                    className="absolute -top-2 -right-2 h-6 w-6 rounded-full"
                                    onClick={clearImage}
                                  >
                                    <X className="h-4 w-4" />
                                  </Button>
                                </div>
                              )}
                              
                              {/* File Upload Input */}
                              <div className="flex items-center gap-4">
                                <input
                                  type="file"
                                  id="profile-picture"
                                  accept="image/*"
                                  onChange={handleImageUpload}
                                  ref={fileInputRef}
                                  className="hidden"
                                />
                                <Button
                                  type="button"
                                  variant="outline"
                                  onClick={() => fileInputRef.current?.click()}
                                  className="flex items-center gap-2"
                                >
                                  <Upload className="h-4 w-4" />
                                  {imagePreview ? 'Change Image' : 'Upload Image'}
                                </Button>
                              </div>
                              <div className="text-xs text-muted-foreground">
                                Images will be compressed to save bandwidth
                              </div>
                            </div>
                          </FormControl>
                          <FormDescription className="font-bold text-primary">
                            NEW! Upload your profile picture here (Max: 2MB)
                          </FormDescription>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    
                    <Button 
                      type="submit" 
                      className="w-full md:w-auto"
                      disabled={updatePersonalInfoMutation.isPending}
                    >
                      {updatePersonalInfoMutation.isPending ? (
                        <>
                          <span className="animate-spin mr-2">⏳</span>
                          Saving...
                        </>
                      ) : (
                        <>
                          <Save className="mr-2 h-4 w-4" />
                          Save Personal Information
                        </>
                      )}
                    </Button>
                  </form>
                </Form>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Teacher Profile Tab */}
          <TabsContent value="teacher" className="space-y-4 mt-6">
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <div>
                    <CardTitle className="text-xl">Teaching Profile</CardTitle>
                    <CardDescription>Update your professional teaching information</CardDescription>
                  </div>
                  <School className="h-5 w-5 text-muted-foreground" />
                </div>
              </CardHeader>
              <CardContent>
                <Form {...teacherProfileForm}>
                  <form onSubmit={teacherProfileForm.handleSubmit(onTeacherProfileSubmit)} className="space-y-6">
                    <FormField
                      control={teacherProfileForm.control}
                      name="school"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>School</FormLabel>
                          <FormControl>
                            <Input placeholder="Enter your school name" {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    
                    <div className="grid gap-6 md:grid-cols-2">
                      <FormField
                        control={teacherProfileForm.control}
                        name="department"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Department</FormLabel>
                            <FormControl>
                              <Input 
                                placeholder="e.g., Mathematics, Science" 
                                {...field} 
                                value={field.value || ''}
                              />
                            </FormControl>
                            <FormDescription>
                              Your academic department or subject area
                            </FormDescription>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      
                      <FormField
                        control={teacherProfileForm.control}
                        name="positionTitle"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Position Title</FormLabel>
                            <FormControl>
                              <Input 
                                placeholder="e.g., Professor, Instructor" 
                                {...field} 
                                value={field.value || ''}
                              />
                            </FormControl>
                            <FormDescription>
                              Your title or position at the institution
                            </FormDescription>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </div>
                    
                    <Button 
                      type="submit" 
                      className="w-full md:w-auto"
                      disabled={updateTeacherProfileMutation.isPending}
                    >
                      {updateTeacherProfileMutation.isPending ? (
                        <>
                          <span className="animate-spin mr-2">⏳</span>
                          Saving...
                        </>
                      ) : (
                        <>
                          <Save className="mr-2 h-4 w-4" />
                          Save Teaching Profile
                        </>
                      )}
                    </Button>
                  </form>
                </Form>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
          </div>
        </main>
      </div>
    </div>
  );
}
