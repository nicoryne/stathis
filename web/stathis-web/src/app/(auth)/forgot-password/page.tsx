'use client';

import type React from 'react';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';
import { motion } from 'framer-motion';
import { CheckCircle2 } from 'lucide-react';
import Link from 'next/link';

export default function ForgotPasswordPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // Simulate API request
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Here you would typically send a password reset email
    setIsLoading(false);
    setIsSubmitted(true);
  };

  return (
    <>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <h1 className="mb-2 text-2xl font-bold tracking-tight">Reset your password</h1>
        <p className="text-muted-foreground">Enter your email to receive a password reset link</p>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.1 }}
      >
        <Card className="border-border/40 mt-6">
          <CardContent className="pt-6">
            {!isSubmitted ? (
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="name@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={isLoading}
                  />
                </div>

                <Button type="submit" className="w-full" disabled={isLoading}>
                  {isLoading ? 'Sending link...' : 'Send reset link'}
                </Button>
              </form>
            ) : (
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="py-4 text-center"
              >
                <div className="mb-4 flex justify-center">
                  <CheckCircle2 className="text-primary h-12 w-12" />
                </div>
                <h3 className="mb-2 text-lg font-medium">Check your email</h3>
                <p className="text-muted-foreground mb-4">
                  We've sent a password reset link to{' '}
                  <span className="text-foreground font-medium">{email}</span>
                </p>
                <Button variant="outline" className="w-full" onClick={() => setIsSubmitted(false)}>
                  Back to reset password
                </Button>
              </motion.div>
            )}
          </CardContent>
        </Card>

        <div className="mt-6 text-center">
          <Link
            href="/login"
            className="text-muted-foreground hover:text-primary text-sm transition-colors"
          >
            Back to login
          </Link>
        </div>
      </motion.div>
    </>
  );
}
