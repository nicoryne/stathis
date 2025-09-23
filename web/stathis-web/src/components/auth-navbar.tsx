'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Bell } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import ThemeSwitcher from '@/components/theme-switcher';
import { getUserDetails, signOut } from '@/services/api-auth-client';
import { getCurrentUserEmail } from '@/lib/utils/jwt';

export function AuthNavbar() {
  const router = useRouter();
  const userEmail = getCurrentUserEmail();
  
  // User details state
  const [userDetails, setUserDetails] = useState({
    first_name: '',
    last_name: '',
    email: userEmail || ''
  });
  
  // Fetch user details
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const user = await getUserDetails();
        
        if (user && typeof user === 'object') {
          // Type assertion to allow property access
          const userObj = user as Record<string, any>;
          setUserDetails({
            first_name: userObj.first_name || '',
            last_name: userObj.last_name || '',
            email: userObj.email || '',
            ...userObj // Include any other properties
          });
        }
      } catch (error) {
        console.error('Error fetching user details:', error);
      }
    };
    fetchUser();
  }, []);
  
  // Handle sign out
  const handleSignOut = async () => {
    try {
      await signOut();
      router.push('/login');
    } catch (error) {
      console.error('Error during sign out:', error);
    }
  };

  return (
    <header className="bg-background border-b">
      <div className="flex h-16 items-center justify-end gap-4 px-4">
        <Button variant="outline" size="icon">
          <Bell className="h-5 w-5" />
        </Button>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="relative h-8 w-8 rounded-full">
              <Avatar className="h-8 w-8">
                <AvatarImage src="/placeholder.svg" alt="User" />
                <AvatarFallback>
                  {userDetails.first_name.charAt(0).toUpperCase() || userEmail?.charAt(0).toUpperCase() || 'U'}
                  {userDetails.last_name.charAt(0).toUpperCase() || ''}
                </AvatarFallback>
              </Avatar>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="w-56" align="end" forceMount>
            <DropdownMenuLabel className="font-normal">
              <div className="flex flex-col space-y-1">
                <p className="text-sm leading-none font-medium">
                  {userDetails.first_name || userEmail || 'User'}
                </p>
                <p className="text-muted-foreground text-xs leading-none">
                  {userDetails.email || userEmail || ''}
                </p>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => router.push('/profile')}>Profile</DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={handleSignOut}>Sign out</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
        <ThemeSwitcher />
      </div>
    </header>
  );
}
