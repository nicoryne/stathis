"use client";

import { SunIcon, MoonIcon } from 'lucide-react';
import { useTheme } from 'next-themes';
import { useEffect, useState } from 'react';

export default function ThemeSwitcher() {
  const { setTheme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = useState(false);

  // Only show the theme switcher after component mounts to avoid hydration errors
  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) {
    return <div className="h-auto w-6" />; // Placeholder with same dimensions
  }

  return (
    <>
      <button onClick={() => setTheme(resolvedTheme === 'dark' ? 'light' : 'dark')}>
        {resolvedTheme === 'dark' ? (
          <MoonIcon className="h-auto w-6" />
        ) : (
          <SunIcon className="h-auto w-6" />
        )}
      </button>
    </>
  );
}
