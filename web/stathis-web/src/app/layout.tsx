import type React from 'react';
import type { Metadata } from 'next';
import { Outfit, Inter } from 'next/font/google';
import './globals.css';
import { ThemeProvider } from '@/components/theme-provider';
import { useReducedMotion } from '@/hooks/use-reduced-motion';
import { QueryProvider } from '@/providers/query-provider';
import { Toaster } from 'sonner';
import { AuthInitializer } from '@/components/auth/auth-initializer';

const outfit = Outfit({
  variable: '--font-outfit',
  subsets: ['latin']
});

const inter = Inter({
  variable: '--font-inter',
  subsets: ['latin']
});

export const metadata: Metadata = {
  title: 'Stathis | Partner in Safe Physical Education',
  description: 'AI-Powered Posture and Vitals Monitoring for Safe Physical Education'
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={`${outfit.variable} ${inter.variable} antialiased`}>
        <QueryProvider>
          <ThemeProvider
            attribute="class"
            defaultTheme="light"
            enableSystem
            disableTransitionOnChange
          >
            {/* Clean up duplicate tokens on app initialization */}
            <AuthInitializer />
            {children}
            <Toaster />
          </ThemeProvider>
        </QueryProvider>
      </body>
    </html>
  );
}
