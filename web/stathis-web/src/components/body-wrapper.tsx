'use client';

import { ReactNode, useEffect, useState } from 'react';

interface BodyWrapperProps {
  children: ReactNode;
  className: string;
}

export function BodyWrapper({ children, className }: BodyWrapperProps) {
  const [mounted, setMounted] = useState(false);

  // Wait for client-side rendering before applying classes
  useEffect(() => {
    setMounted(true);
  }, []);

  // During SSR, just render with the basic class to match what the server did
  // After hydration, this effect will run and it will re-render with the actual classes
  if (!mounted) {
    return <div className={className}>{children}</div>;
  }

  return <div className={className}>{children}</div>;
} 