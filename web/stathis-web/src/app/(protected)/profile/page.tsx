'use client';

import React, { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import * as z from 'zod';
import { motion } from 'framer-motion';
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
  X,
  HeartPulse,
  Sparkles
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
      <div className="flex min-h-screen relative overflow-hidden">
        {/* Animated Background Particles */}
        <div className="absolute inset-0 -z-10">
          <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-secondary/5 to-accent/5" />
          {[...Array(20)].map((_, i) => (
            <motion.div
              key={i}
              className="absolute w-2 h-2 bg-primary/20 rounded-full"
              style={{
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 100}%`,
              }}
              animate={{
                y: [0, -20, 0],
                opacity: [0.3, 0.8, 0.3],
              }}
              transition={{
                duration: 3 + Math.random() * 2,
                repeat: Infinity,
                delay: Math.random() * 2,
              }}
            />
          ))}
        </div>

        <Sidebar className="w-64 flex-shrink-0" />
        <div className="flex-1">
          <AuthNavbar />
          <main className="p-6 relative">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              className="mb-8"
            >
              <div className="flex items-center gap-4 mb-4">
                <div className="relative">
                  <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg" />
                  <HeartPulse className="relative h-8 w-8 text-primary" />
                </div>
                <div>
                  <h1 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                    Profile Management
                  </h1>
                  <p className="text-muted-foreground mt-1">Manage your profile information and settings</p>
                </div>
              </div>
            </motion.div>
            
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
            >
              <Card className="mx-auto max-w-lg rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg">
                <CardHeader>
                  <CardTitle className="flex items-center gap-3">
                    <div className="p-2 rounded-full bg-primary/10">
                      <HeartPulse className="h-5 w-5 text-primary animate-pulse" />
                    </div>
                    Loading...
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex justify-center p-8">
                    <div className="flex flex-col items-center gap-4">
                      <div className="relative">
                        <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg animate-pulse" />
                        <div className="relative w-16 h-16 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 animate-spin" />
                      </div>
                      <p className="text-muted-foreground font-medium">Loading your profile information...</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          </main>
        </div>
      </div>
    );
  }
  
  return (
    <div className="flex min-h-screen relative overflow-hidden">
      {/* Animated Background Particles */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-secondary/5 to-accent/5" />
        {[...Array(20)].map((_, i) => (
          <motion.div
            key={i}
            className="absolute w-2 h-2 bg-primary/20 rounded-full"
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
            }}
            animate={{
              y: [0, -20, 0],
              opacity: [0.3, 0.8, 0.3],
            }}
            transition={{
              duration: 3 + Math.random() * 2,
              repeat: Infinity,
              delay: Math.random() * 2,
            }}
          />
        ))}
      </div>

      <Sidebar className="w-64 flex-shrink-0" />
      
      <div className="flex-1">
        <AuthNavbar />
        
        <main className="p-6 relative">
          {/* Welcome Section */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="mb-8"
          >
            <div className="flex items-center gap-4 mb-4">
              <div className="relative">
                <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg" />
                <HeartPulse className="relative h-8 w-8 text-primary" />
              </div>
              <div>
                <h1 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                  Profile Management
                </h1>
                <p className="text-muted-foreground mt-1">Manage your profile information and settings</p>
              </div>
            </div>
            
            <div className="flex items-center gap-4">
              <Button 
                variant="outline" 
                size="sm"
                onClick={() => queryClient.invalidateQueries({ queryKey: ['teacher-profile'] })}
                className="bg-card/80 backdrop-blur-xl border-border/50 hover:bg-card/90"
              >
                <Sparkles className="h-4 w-4 mr-2" />
                Refresh
              </Button>
            </div>
          </motion.div>

          <div className="grid gap-8">
        {/* Profile Summary */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.1 }}
        >
          <Card className="rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300">
            <CardContent className="p-8">
              <div className="flex flex-col md:flex-row gap-8 items-center md:items-start">
                <div className="flex-shrink-0 relative">
                  <div className="relative">
                    <div className="absolute -inset-4 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-xl" />
                    <Avatar className="relative h-32 w-32 border-4 border-primary/20 shadow-xl">
                      <AvatarImage src={profileData?.profilePictureUrl || ''} alt={profileData?.firstName} />
                      <AvatarFallback className="text-2xl font-bold bg-gradient-to-br from-primary/20 to-secondary/20">
                        {profileData?.firstName?.charAt(0)}{profileData?.lastName?.charAt(0)}
                      </AvatarFallback>
                    </Avatar>
                  </div>
                </div>
                <div className="flex-grow space-y-4 text-center md:text-left">
                  <div>
                    <h2 className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                      {profileData?.firstName} {profileData?.lastName}
                    </h2>
                    <div className="flex flex-col md:flex-row gap-6 text-muted-foreground mt-4">
                      <div className="flex items-center gap-2 justify-center md:justify-start">
                        <div className="p-2 rounded-full bg-primary/10">
                          <Mail className="h-4 w-4 text-primary" />
                        </div>
                        <span className="font-medium">{profileData?.email}</span>
                      </div>
                      {profileData?.school && (
                        <div className="flex items-center gap-2 justify-center md:justify-start">
                          <div className="p-2 rounded-full bg-secondary/10">
                            <School className="h-4 w-4 text-secondary" />
                          </div>
                          <span className="font-medium">{profileData.school}</span>
                        </div>
                      )}
                      {profileData?.positionTitle && (
                        <div className="flex items-center gap-2 justify-center md:justify-start">
                          <div className="p-2 rounded-full bg-accent/10">
                            <Award className="h-4 w-4 text-accent" />
                          </div>
                          <span className="font-medium">{profileData.positionTitle}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>

        {/* Profile Edit Forms */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
        >
          <Tabs defaultValue="personal" value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="grid w-full grid-cols-2 max-w-md bg-card/80 backdrop-blur-xl border-border/50 rounded-xl">
              <TabsTrigger 
                value="personal" 
                className="rounded-lg data-[state=active]:bg-primary/10 data-[state=active]:text-primary data-[state=active]:border data-[state=active]:border-primary/20"
              >
                Personal Information
              </TabsTrigger>
              <TabsTrigger 
                value="teacher"
                className="rounded-lg data-[state=active]:bg-primary/10 data-[state=active]:text-primary data-[state=active]:border data-[state=active]:border-primary/20"
              >
                Teaching Profile
              </TabsTrigger>
            </TabsList>

          {/* Personal Information Tab */}
          <TabsContent value="personal" className="space-y-4 mt-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.3 }}
            >
              <Card className="rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300">
                <CardHeader>
                  <div className="flex justify-between items-center">
                    <div>
                      <CardTitle className="text-xl flex items-center gap-3">
                        <div className="p-2 rounded-full bg-primary/10">
                          <UserIcon className="h-5 w-5 text-primary" />
                        </div>
                        Personal Information
                      </CardTitle>
                      <CardDescription>Update your personal details</CardDescription>
                    </div>
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
                          <FormLabel className="text-base font-semibold">Profile Picture</FormLabel>
                          <FormControl>
                            <div className="space-y-6">
                              {/* Image Preview */}
                              {imagePreview && (
                                <motion.div 
                                  initial={{ opacity: 0, scale: 0.8 }}
                                  animate={{ opacity: 1, scale: 1 }}
                                  transition={{ duration: 0.3 }}
                                  className="relative w-40 h-40 mx-auto md:mx-0"
                                >
                                  <div className="relative">
                                    <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg" />
                                    <img 
                                      src={imagePreview} 
                                      alt="Profile preview" 
                                      className="relative w-full h-full object-cover rounded-full border-4 border-primary/30 shadow-xl"
                                    />
                                  </div>
                                  <Button
                                    type="button"
                                    variant="destructive"
                                    size="icon"
                                    className="absolute -top-2 -right-2 h-8 w-8 rounded-full shadow-lg hover:shadow-xl transition-all duration-200"
                                    onClick={clearImage}
                                  >
                                    <X className="h-4 w-4" />
                                  </Button>
                                </motion.div>
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
                                  className="flex items-center gap-2 bg-card/80 backdrop-blur-xl border-border/50 hover:bg-card/90 hover:border-primary/50 transition-all duration-200"
                                >
                                  <Upload className="h-4 w-4" />
                                  {imagePreview ? 'Change Image' : 'Upload Image'}
                                </Button>
                              </div>
                              <div className="text-sm text-muted-foreground bg-muted/50 rounded-lg p-3">
                                <div className="flex items-center gap-2 mb-1">
                                  <Sparkles className="h-4 w-4 text-primary" />
                                  <span className="font-medium">Upload Guidelines</span>
                                </div>
                                <p>Images will be compressed to save bandwidth. Maximum file size: 5MB</p>
                              </div>
                            </div>
                          </FormControl>
                          <FormDescription className="font-bold text-primary bg-primary/10 rounded-lg p-2">
                            ✨ NEW! Upload your profile picture here (Max: 5MB)
                          </FormDescription>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    
                    <motion.div
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.6, delay: 0.4 }}
                    >
                      <Button 
                        type="submit" 
                        className="w-full md:w-auto bg-gradient-to-r from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 shadow-lg hover:shadow-xl transition-all duration-200"
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
                    </motion.div>
                  </form>
                </Form>
              </CardContent>
            </Card>
            </motion.div>
          </TabsContent>

          {/* Teacher Profile Tab */}
          <TabsContent value="teacher" className="space-y-4 mt-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.5 }}
            >
              <Card className="rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300">
                <CardHeader>
                  <div className="flex justify-between items-center">
                    <div>
                      <CardTitle className="text-xl flex items-center gap-3">
                        <div className="p-2 rounded-full bg-secondary/10">
                          <School className="h-5 w-5 text-secondary" />
                        </div>
                        Teaching Profile
                      </CardTitle>
                      <CardDescription>Update your professional teaching information</CardDescription>
                    </div>
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
                    
                    <motion.div
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.6, delay: 0.6 }}
                    >
                      <Button 
                        type="submit" 
                        className="w-full md:w-auto bg-gradient-to-r from-secondary to-accent hover:from-secondary/90 hover:to-accent/90 shadow-lg hover:shadow-xl transition-all duration-200"
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
                    </motion.div>
                  </form>
                </Form>
              </CardContent>
            </Card>
            </motion.div>
          </TabsContent>
        </Tabs>
        </motion.div>
          </div>
        </main>
      </div>
    </div>
  );
}
