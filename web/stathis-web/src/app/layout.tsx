import type React from 'react';
import type { Metadata } from 'next';
import { Outfit, Inter } from 'next/font/google';
import './globals.css';
import { ThemeProvider } from '@/components/theme-provider';
import { QueryProvider } from '@/providers/query-provider';
import { Toaster } from 'sonner';
import { DbInitializer } from '@/components/db-initializer';

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
      <body suppressHydrationWarning>
        <QueryProvider>
          <ThemeProvider
            attribute="class"
            defaultTheme="light"
            enableSystem
            disableTransitionOnChange
          >
            <div className={`${outfit.variable} ${inter.variable} antialiased`}>
              <DbInitializer />
              {children}
              <Toaster />
            </div>
          </ThemeProvider>
        </QueryProvider>
      </body>
    </html>
  );
}
