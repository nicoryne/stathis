"use client";

import { useState, useRef } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { Loader2, Settings, Bell, Upload, User, School, BookOpen, GraduationCap } from "lucide-react";

import { userProfileSchema, UserProfileFormValues } from "@/lib/validations/user-profile";
import { getUserProfile, updateUserProfile } from "@/services/user-profile";
import { getUserDetails, logout } from '@/services/auth';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Sidebar } from "@/components/dashboard/sidebar";
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";
import ThemeSwitcher from '@/components/theme-switcher';
import { createBrowserClient } from '@supabase/ssr';

export default function SettingsPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [userDetails, setUserDetails] = useState({
    first_name: '',
    last_name: '',
    email: ''
  });
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);

  const form = useForm<UserProfileFormValues>({
    resolver: zodResolver(userProfileSchema),
    defaultValues: {
      first_name: "",
      last_name: "",
      email: "",
      user_role: "student",
      picture_url: "",
      school_attending: "",
      year_level: "",
      course_enrolled: "",
    },
    mode: "onChange"
  });

  useEffect(() => {
    const fetchUser = async () => {
      const user = await getUserDetails();

      if (user) {
        setUserDetails({
          first_name: user.first_name,
          last_name: user.last_name,
          email: user.email
        });
      }
    };
    fetchUser();
  }, []);

  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        setIsLoading(true);
        const userProfile = await getUserProfile();
        if (userProfile) {
          form.reset({
            first_name: userProfile.first_name || "",
            last_name: userProfile.last_name || "",
            email: userProfile.email || "",
            user_role: userProfile.user_role || "student",
            picture_url: userProfile.picture_url || "",
            school_attending: userProfile.school_attending || "",
            year_level: userProfile.year_level || "",
            course_enrolled: userProfile.course_enrolled || "",
          });
          
          if (userProfile.picture_url) {
            setImagePreview(userProfile.picture_url);
          }
        }
      } catch (error) {
        console.error("Error loading user profile:", error);
        toast.error("Failed to load user profile");
      } finally {
        setIsLoading(false);
      }
    };

    loadUserProfile();
  }, [form]);

  // Handle file upload to Supabase Storage
  const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Check file type
    if (!file.type.includes('image/jpeg') && !file.type.includes('image/png')) {
      toast.error('Only JPEG and PNG files are allowed');
      return;
    }

    // Check file size (max 2MB)
    if (file.size > 2 * 1024 * 1024) {
      toast.error('File size should be less than 2MB');
      return;
    }

    try {
      setIsUploading(true);
      
      // Create a local preview
      const objectUrl = URL.createObjectURL(file);
      setImagePreview(objectUrl);

      // Upload to Supabase
      const supabase = createBrowserClient(
        process.env.NEXT_PUBLIC_SUPABASE_URL || '',
        process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
      );

      const { data: { user } } = await supabase.auth.getUser();
      if (!user) throw new Error('User not authenticated');

      // Delete old image if exists
      const currentPictureUrl = form.getValues('picture_url');
      if (currentPictureUrl) {
        try {
          console.log('Found existing picture URL:', currentPictureUrl);
          
          // Extract the path components from the URL
          // URL format is like: https://ehigwmhdzgpgdogtgsry.supabase.co/storage/v1/object/public/user-avatars/user-id/filename.ext
          // We need to extract just "user-id/filename.ext"
          
          // Check if this is a Supabase URL
          if (currentPictureUrl.includes('supabase.co/storage')) {
            // Split the URL by "user-avatars/"
            const parts = currentPictureUrl.split('user-avatars/');
            if (parts.length > 1) {
              // The path will be everything after "user-avatars/"
              const pathToDelete = parts[1];
              
              console.log('Attempting to delete:', pathToDelete);
              
              // Delete the file
              const { data, error: deleteError } = await supabase.storage
                .from('user-avatars')
                .remove([pathToDelete]);
                
              if (deleteError) {
                console.error('Failed to delete image:', deleteError);
              } else {
                console.log('Successfully deleted old image:', data);
              }
            } else {
              console.log('Could not parse image path correctly:', parts);
            }
          } else {
            console.log('URL does not appear to be a Supabase storage URL');
          }
        } catch (deleteError) {
          console.error('Error handling image deletion:', deleteError);
          // Continue with upload even if delete fails
        }
      }

      // Create a unique file name
      const fileExt = file.name.split('.').pop();
      const fileName = `${Date.now()}.${fileExt}`;
      
      // Use user ID as folder path to match RLS policies
      const filePath = `${user.id}/${fileName}`;

      // Upload the file
      const { data, error } = await supabase.storage
        .from('user-avatars')
        .upload(filePath, file, {
          cacheControl: '3600',
          upsert: true
        });

      if (error) throw error;

      // Get the public URL
      const { data: { publicUrl } } = supabase.storage
        .from('user-avatars')
        .getPublicUrl(data.path);

      // Set the URL in the form
      form.setValue('picture_url', publicUrl);
      toast.success('Image uploaded successfully');
    } catch (error) {
      console.error('Error uploading file:', error);
      toast.error('Failed to upload image');
      // Revert preview on error
      setImagePreview(form.getValues('picture_url') || null);
    } finally {
      setIsUploading(false);
    }
  };

  // Create a manual submit handler that will be called when the form is submitted
  const handleFormSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    console.log("Form submitted manually");
    
    // Get all form values
    const formValues = form.getValues();
    console.log("Form values:", formValues);
    
    // Check if required fields are filled
    if (!formValues.first_name || !formValues.last_name) {
      toast.error("Please fill in required fields");
      return;
    }
    
    // Ensure user_role is set
    if (!formValues.user_role) {
      formValues.user_role = 'student';
    }
    
    setIsLoading(true);
    
    try {
      // Call the update function
      await updateUserProfile(formValues);
      toast.success("Profile updated successfully");
    } catch (error: any) {
      console.error("Error updating profile:", error);
      toast.error(`Failed to update profile: ${error.message || 'Unknown error'}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    await logout();
  };

  return (
    <div className="flex min-h-screen bg-muted/10">
      <Sidebar className="w-64 flex-shrink-0" />
      
      <div className="flex-1">
        <header className="bg-background border-b sticky top-0 z-10">
          <div className="flex h-16 items-center justify-end gap-4 px-6">
            <Button variant="outline" size="icon" className="rounded-full">
              <Bell className="h-5 w-5" />
              <span className="sr-only">Notifications</span>
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                  <Avatar className="h-8 w-8 border border-muted">
                    <AvatarImage src={imagePreview || "/placeholder.svg"} alt="User" />
                    <AvatarFallback className="bg-primary/10 text-primary">
                      {userDetails.first_name.charAt(0).toUpperCase()}
                      {userDetails.last_name.charAt(0).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end" forceMount>
                <DropdownMenuLabel className="font-normal">
                  <div className="flex flex-col space-y-1">
                    <p className="text-sm leading-none font-medium">
                      {userDetails.first_name} {userDetails.last_name}
                    </p>
                    <p className="text-muted-foreground text-xs leading-none">
                      {userDetails.email}
                    </p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout}>Log out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>

        <div className="p-6 md:p-8 max-w-5xl mx-auto">
          <div className="mb-8 flex items-center">
            <Settings className="h-6 w-6 mr-3 text-primary" />
            <h1 className="text-2xl font-bold">Profile Settings</h1>
          </div>
          
          <Tabs defaultValue="personal" className="w-full">
            <TabsList className="mb-6 grid w-full grid-cols-2 md:w-auto">
              <TabsTrigger value="personal" className="flex items-center gap-2">
                <User className="h-4 w-4" />
                <span>Personal Info</span>
              </TabsTrigger>
              <TabsTrigger value="academic" className="flex items-center gap-2">
                <GraduationCap className="h-4 w-4" />
                <span>Academic</span>
              </TabsTrigger>
            </TabsList>
            
            <Form {...form}>
              <form onSubmit={handleFormSubmit} className="space-y-6">
                <TabsContent value="personal" className="space-y-6">
                  <Card>
                    <CardHeader>
                      <CardTitle>Profile Picture</CardTitle>
                      <CardDescription>
                        Update your profile image
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <FormField
                        control={form.control}
                        name="picture_url"
                        render={({ field }) => (
                          <FormItem>
                            <div className="flex flex-col sm:flex-row sm:items-center gap-6">
                              <Avatar className="h-24 w-24 border-2 border-muted">
                                <AvatarImage src={imagePreview || "/placeholder.svg"} alt="Profile preview" />
                                <AvatarFallback className="bg-primary/10 text-primary text-2xl">
                                  {userDetails.first_name?.charAt(0).toUpperCase() || ''}
                                  {userDetails.last_name?.charAt(0).toUpperCase() || ''}
                                </AvatarFallback>
                              </Avatar>
                              <div className="flex-1">
                                <input
                                  type="file"
                                  accept="image/jpeg,image/png"
                                  onChange={handleFileUpload}
                                  ref={fileInputRef}
                                  className="hidden"
                                />
                                <div className="space-y-2">
                                  <Button 
                                    type="button" 
                                    variant="outline" 
                                    onClick={() => fileInputRef.current?.click()}
                                    disabled={isUploading}
                                    className="h-9"
                                  >
                                    {isUploading ? (
                                      <>
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                        Uploading...
                                      </>
                                    ) : (
                                      <>
                                        <Upload className="mr-2 h-4 w-4" />
                                        Upload New Image
                                      </>
                                    )}
                                  </Button>
                                  <FormControl>
                                    <Input {...field} value={field.value || ''} className="hidden" />
                                  </FormControl>
                                  <FormDescription className="text-xs">
                                    Upload JPG or PNG (max 2MB)
                                  </FormDescription>
                                </div>
                              </div>
                            </div>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle>Personal Information</CardTitle>
                      <CardDescription>
                        Update your personal details
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-6">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <FormField
                          control={form.control}
                          name="first_name"
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>First Name</FormLabel>
                              <FormControl>
                                <Input placeholder="First name" {...field} value={field.value || ''} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                        <FormField
                          control={form.control}
                          name="last_name"
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Last Name</FormLabel>
                              <FormControl>
                                <Input placeholder="Last name" {...field} value={field.value || ''} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                      </div>

                      <FormField
                        control={form.control}
                        name="email"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>Email</FormLabel>
                            <FormControl>
                              <Input placeholder="Email" type="email" {...field} value={field.value || ''} disabled className="bg-muted/40" />
                            </FormControl>
                            <FormDescription>Email cannot be changed</FormDescription>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      
                      <FormField
                        control={form.control}
                        name="user_role"
                        render={({ field }) => (
                          <FormItem className="space-y-3">
                            <FormLabel>User Role</FormLabel>
                            <FormControl>
                              <RadioGroup
                                onValueChange={field.onChange}
                                defaultValue={field.value || "student"}
                                className="flex flex-col space-y-1"
                              >
                                <div className="grid grid-cols-2 gap-4">
                                  <FormItem className="flex items-center space-x-3 space-y-0 rounded-md border p-4">
                                    <FormControl>
                                      <RadioGroupItem value="student" />
                                    </FormControl>
                                    <FormLabel className="font-normal cursor-pointer">
                                      Student
                                    </FormLabel>
                                  </FormItem>
                                  <FormItem className="flex items-center space-x-3 space-y-0 rounded-md border p-4">
                                    <FormControl>
                                      <RadioGroupItem value="teacher" />
                                    </FormControl>
                                    <FormLabel className="font-normal cursor-pointer">
                                      Teacher
                                    </FormLabel>
                                  </FormItem>
                                </div>
                              </RadioGroup>
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </CardContent>
                  </Card>
                </TabsContent>

                <TabsContent value="academic" className="space-y-6">
                  <Card>
                    <CardHeader>
                      <div className="flex items-center gap-2">
                        <School className="h-5 w-5 text-primary" />
                        <CardTitle>Academic Information</CardTitle>
                      </div>
                      <CardDescription>
                        Update your school and academic details
                      </CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-6">
                      <FormField
                        control={form.control}
                        name="school_attending"
                        render={({ field }) => (
                          <FormItem>
                            <FormLabel>School</FormLabel>
                            <FormControl>
                              <Input placeholder="School or institution" {...field} value={field.value || ''} />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <FormField
                          control={form.control}
                          name="year_level"
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Year Level</FormLabel>
                              <FormControl>
                                <Input placeholder="Year level" {...field} value={field.value || ''} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                        <FormField
                          control={form.control}
                          name="course_enrolled"
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Course</FormLabel>
                              <FormControl>
                                <Input placeholder="Course enrolled" {...field} value={field.value || ''} />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                      </div>
                    </CardContent>
                  </Card>
                </TabsContent>
                
                <Separator className="my-8" />
                
                <div className="flex justify-end gap-4">
                  <Button 
                    type="button" 
                    variant="outline" 
                    onClick={() => form.reset()}
                  >
                    Cancel
                  </Button>
                  <Button 
                    type="submit"
                    disabled={isLoading}
                    className="min-w-[120px]"
                  >
                    {isLoading ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Saving...
                      </>
                    ) : (
                      "Save Changes"
                    )}
                  </Button>
                </div>
              </form>
            </Form>
          </Tabs>
        </div>
      </div>
    </div>
  );
}
