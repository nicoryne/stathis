"use client";

import { useState, useEffect } from 'react';
import { SunIcon, MoonIcon } from 'lucide-react';
import { useTheme } from 'next-themes';

// Create a client-only component to prevent hydration errors
function ClientOnlyThemeSwitcher() {
  const { setTheme, resolvedTheme } = useTheme();
  
  return (
    <button onClick={() => setTheme(resolvedTheme === 'dark' ? 'light' : 'dark')}>
      {resolvedTheme === 'dark' ? (
        <MoonIcon className="h-auto w-6" />
      ) : (
        <SunIcon className="h-auto w-6" />
      )}
    </button>
  );
}

export default function ThemeSwitcher() {
  const [isMounted, setIsMounted] = useState(false);
  
  useEffect(() => {
    setIsMounted(true);
  }, []);
  
  // Return a fixed placeholder during SSR and initial client render
  // This ensures consistent rendering between server and client
  return isMounted ? (
    <ClientOnlyThemeSwitcher />
  ) : (
    // Empty button with same dimensions to avoid layout shift
    <button className="h-auto w-6" aria-hidden="true" />
  );
}
