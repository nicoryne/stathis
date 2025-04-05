'use client';

import type React from 'react';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardFooter } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Github, Mail } from 'lucide-react';
import { motion } from 'framer-motion';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

export default function RegisterPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const router = useRouter();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    // Clear error when user types
    if (errors[name]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Name is required';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }

    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    // Simulate registration delay
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Here you would typically handle registration with your backend
    // For now, we'll just redirect to the login page
    setIsLoading(false);
    router.push('/login');
  };

  return (
    <>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <h1 className="mb-2 text-2xl font-bold tracking-tight">Create an account</h1>
        <p className="text-muted-foreground">Sign up for Stathis to get started</p>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.1 }}
      >
        <Card className="border-border/40 mt-6">
          <CardContent className="pt-6">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Name</Label>
                <Input
                  id="name"
                  name="name"
                  placeholder="John Doe"
                  value={formData.name}
                  onChange={handleChange}
                  disabled={isLoading}
                />
                {errors.name && <p className="text-destructive mt-1 text-xs">{errors.name}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="name@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  disabled={isLoading}
                />
                {errors.email && <p className="text-destructive mt-1 text-xs">{errors.email}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                  disabled={isLoading}
                />
                {errors.password && (
                  <p className="text-destructive mt-1 text-xs">{errors.password}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirm Password</Label>
                <Input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  disabled={isLoading}
                />
                {errors.confirmPassword && (
                  <p className="text-destructive mt-1 text-xs">{errors.confirmPassword}</p>
                )}
              </div>

              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading ? 'Creating account...' : 'Create account'}
              </Button>

              <p className="text-muted-foreground text-center text-xs">
                By clicking "Create account", you agree to our{' '}
                <a href="#" className="hover:text-primary underline underline-offset-4">
                  Terms of Service
                </a>{' '}
                and{' '}
                <a href="#" className="hover:text-primary underline underline-offset-4">
                  Privacy Policy
                </a>
                .
              </p>
            </form>
          </CardContent>

          <div className="px-6 py-2">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <Separator />
              </div>
              <div className="relative flex justify-center">
                <span className="bg-card text-muted-foreground px-2 text-xs">Or continue with</span>
              </div>
            </div>
          </div>

          <CardFooter className="flex flex-col gap-2 pt-2 pb-6">
            <Button variant="outline" className="w-full" disabled={isLoading}>
              <Github className="mr-2 h-4 w-4" />
              GitHub
            </Button>
            <Button variant="outline" className="w-full" disabled={isLoading}>
              <Mail className="mr-2 h-4 w-4" />
              Google
            </Button>
          </CardFooter>
        </Card>

        <div className="mt-6 text-center">
          <Link
            href="/login"
            className="text-muted-foreground hover:text-primary text-sm transition-colors"
          >
            Already have an account? Sign in
          </Link>
        </div>
      </motion.div>
    </>
  );
}
