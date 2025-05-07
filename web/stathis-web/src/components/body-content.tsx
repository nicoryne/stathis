'use client';

import { ReactNode, useEffect, useState } from 'react';

interface BodyContentProps {
  children: ReactNode;
  fontVariables: string;
}

export function BodyContent({ children, fontVariables }: BodyContentProps) {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // Apply classes after hydration
    setMounted(true);
  }, []);

  if (!mounted) {
    // On first render, don't return anything visible - just a placeholder
    return <div style={{ visibility: 'hidden' }}></div>;
  }

  return (
    <div className={`${fontVariables} antialiased`}>
      {children}
    </div>
  );
} 