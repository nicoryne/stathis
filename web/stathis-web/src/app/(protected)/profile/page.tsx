'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import * as z from 'zod';
import { DashboardShell } from '@/components/dashboard/dashboard-shell';
import { DashboardHeader } from '@/components/dashboard/dashboard-header';
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
import { useToast } from '@/components/ui/use-toast';
import { 
  UserIcon, 
  School, 
  Building2, 
  Award, 
  Calendar,
  Mail,
  Save
} from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { 
  getTeacherProfile, 
  updateUserProfile, 
  updateTeacherProfile, 
  UserProfileDTO 
} from '@/services/users/api-user-client';

// Define validation schemas for forms
const personalInfoSchema = z.object({
  firstName: z.string().min(2, { message: 'First name must be at least 2 characters.' }),
  lastName: z.string().min(2, { message: 'Last name must be at least 2 characters.' }),
  birthdate: z.string().optional(),
  profilePictureUrl: z.string().url({ message: 'Please enter a valid URL.' }).optional().or(z.literal('')),
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
  
  // Fetch teacher profile data
  const { 
    data: profileData, 
    isLoading, 
    isError, 
    error 
  } = useQuery({
    queryKey: ['teacher-profile'],
    queryFn: getTeacherProfile,
  });
  
  // Initialize personal info form
  const personalInfoForm = useForm<z.infer<typeof personalInfoSchema>>({
    resolver: zodResolver(personalInfoSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      birthdate: '',
      profilePictureUrl: '',
    },
  });
  
  // Initialize teacher profile form
  const teacherProfileForm = useForm<z.infer<typeof teacherProfileSchema>>({
    resolver: zodResolver(teacherProfileSchema),
    defaultValues: {
      school: '',
      department: '',
      positionTitle: '',
    },
  });
  
  // Update form values when profile data is loaded
  React.useEffect(() => {
    if (profileData) {
      personalInfoForm.reset({
        firstName: profileData.firstName,
        lastName: profileData.lastName,
        birthdate: profileData.birthdate || '',
        profilePictureUrl: profileData.profilePictureUrl || '',
      });
      
      teacherProfileForm.reset({
        school: profileData.school || '',
        department: profileData.department || '',
        positionTitle: profileData.positionTitle || '',
      });
    }
  }, [profileData, personalInfoForm, teacherProfileForm]);
  
  // Mutation for updating personal info
  const updatePersonalInfoMutation = useMutation({
    mutationFn: updateUserProfile,
    onSuccess: (data) => {
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
    mutationFn: updateTeacherProfile,
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
    updatePersonalInfoMutation.mutate(values);
  };
  
  const onTeacherProfileSubmit = (values: z.infer<typeof teacherProfileSchema>) => {
    updateTeacherProfileMutation.mutate(values);
  };
  
  // Handle loading and error states
  if (isLoading) {
    return (
      <DashboardShell>
        <DashboardHeader heading="Profile Management" text="Manage your profile information" />
        <div className="flex items-center justify-center py-12">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
            <p className="text-muted-foreground">Loading your profile...</p>
          </div>
        </div>
      </DashboardShell>
    );
  }
  
  if (isError) {
    return (
      <DashboardShell>
        <DashboardHeader heading="Profile Management" text="Manage your profile information" />
        <Card className="mx-auto max-w-lg">
          <CardHeader>
            <CardTitle className="text-red-500">Error Loading Profile</CardTitle>
            <CardDescription>
              There was a problem loading your profile information.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">
              {error instanceof Error ? error.message : 'Unknown error occurred'}
            </p>
            <Button 
              className="mt-4" 
              onClick={() => queryClient.invalidateQueries({ queryKey: ['teacher-profile'] })}
            >
              Try Again
            </Button>
          </CardContent>
        </Card>
      </DashboardShell>
    );
  }
  
  return (
    <DashboardShell>
      <DashboardHeader heading="Profile Management" text="Manage your profile information">
        <div className="flex items-center gap-4">
          <Button 
            variant="outline" 
            size="sm"
            onClick={() => queryClient.invalidateQueries({ queryKey: ['teacher-profile'] })}
          >
            Refresh
          </Button>
        </div>
      </DashboardHeader>

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
                          <FormLabel>Profile Picture URL</FormLabel>
                          <FormControl>
                            <Input 
                              placeholder="https://example.com/profile-picture.jpg" 
                              {...field} 
                            />
                          </FormControl>
                          <FormDescription>
                            Enter a direct URL to an image. Leave blank to use initials.
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
    </DashboardShell>
  );
}
