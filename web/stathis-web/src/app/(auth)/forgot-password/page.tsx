'use client';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { motion } from 'framer-motion';
import {
  CheckCircle2,
  Loader2,
  HeartPulse,
  Activity,
  Shield,
  Users,
  ArrowLeft
} from 'lucide-react';
import Link from 'next/link';
import { useMutation } from '@tanstack/react-query';
import { forgotPassword } from '@/services/auth';
import { forgotPasswordSchema, type ForgotPasswordFormValues } from '@/lib/validations/auth';
import { useFormValidation } from '@/hooks/use-form-validation';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { toast } from 'sonner';

export default function ForgotPasswordPage() {
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [submittedEmail, setSubmittedEmail] = useState('');

  const form = useFormValidation(forgotPasswordSchema, {
    email: ''
  });

  const forgotPasswordMutation = useMutation({
    mutationFn: forgotPassword,
    onSuccess: () => {
      setSubmittedEmail(form.getValues().email);
      setIsSubmitted(true);
      toast.success('Reset link sent', {
        description: 'Check your email for the password reset link'
      });
    },
    onError: (error) => {
      toast.error('Failed to send reset link', {
        description: error.message || 'Please check your email and try again'
      });
    }
  });

  const onSubmit = (data: ForgotPasswordFormValues) => {
    forgotPasswordMutation.mutate(data);
  };

  return (
    <div className="flex min-h-screen flex-col md:flex-row">
      {/* Left Panel - Brand Section */}
      <div className="from-primary/90 to-secondary/90 relative hidden overflow-hidden bg-gradient-to-br p-8 text-white md:flex md:w-1/2">
        {/* Animated background elements */}
        <div className="absolute top-0 left-0 h-full w-full overflow-hidden">
          <motion.div
            className="absolute top-10 left-10 h-64 w-64 rounded-full bg-white/10"
            animate={{
              scale: [1, 1.2, 1],
              x: [0, 30, 0],
              y: [0, 50, 0]
            }}
            transition={{
              duration: 15,
              repeat: Number.POSITIVE_INFINITY,
              repeatType: 'reverse'
            }}
          />
          <motion.div
            className="absolute right-10 bottom-20 h-80 w-80 rounded-full bg-white/5"
            animate={{
              scale: [1, 1.1, 1],
              x: [0, -20, 0],
              y: [0, -30, 0]
            }}
            transition={{
              duration: 20,
              repeat: Number.POSITIVE_INFINITY,
              repeatType: 'reverse'
            }}
          />
        </div>

        <div className="relative z-10 flex h-full flex-col">
          <div className="mb-12 flex items-center gap-2">
            <HeartPulse className="h-8 w-8 text-white" />
            <span className="text-2xl font-bold tracking-tight">Stathis</span>
          </div>

          <div className="my-auto">
            <motion.h1
              className="mb-6 text-3xl font-bold"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
            >
              Account Recovery
            </motion.h1>
            <motion.p
              className="mb-12 max-w-md text-lg text-white/90"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 }}
            >
              We'll help you reset your password and get back to monitoring your students' safety
            </motion.p>

            <div className="space-y-6">
              <motion.div
                className="flex items-start gap-4"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.5, delay: 0.2 }}
              >
                <div className="rounded-full bg-white/20 p-2">
                  <Activity className="h-5 w-5" />
                </div>
                <div>
                  <h3 className="mb-1 font-medium">Simple Recovery</h3>
                  <p className="text-sm text-white/80">
                    Reset your password with just a few clicks
                  </p>
                </div>
              </motion.div>

              <motion.div
                className="flex items-start gap-4"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.5, delay: 0.3 }}
              >
                <div className="rounded-full bg-white/20 p-2">
                  <Shield className="h-5 w-5" />
                </div>
                <div>
                  <h3 className="mb-1 font-medium">Secure Process</h3>
                  <p className="text-sm text-white/80">Your account security is our top priority</p>
                </div>
              </motion.div>

              <motion.div
                className="flex items-start gap-4"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.5, delay: 0.4 }}
              >
                <div className="rounded-full bg-white/20 p-2">
                  <Users className="h-5 w-5" />
                </div>
                <div>
                  <h3 className="mb-1 font-medium">Support Available</h3>
                  <p className="text-sm text-white/80">
                    Our team is ready to help if you need assistance
                  </p>
                </div>
              </motion.div>
            </div>
          </div>

          <div className="mt-auto text-sm text-white/70">
            &copy; {new Date().getFullYear()} Stathis. All rights reserved.
          </div>
        </div>
      </div>

      {/* Right Panel - Forgot Password Form */}
      <div className="flex w-full items-center justify-center p-8 md:w-1/2">
        <div className="w-full max-w-md">
          {/* Mobile Logo - Only visible on mobile */}
          <div className="mb-8 flex items-center justify-center md:hidden">
            <div className="flex items-center gap-2">
              <HeartPulse className="text-primary h-8 w-8" />
              <span className="text-2xl font-bold tracking-tight">Stathis</span>
            </div>
          </div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="mb-8"
          >
            <h1 className="mb-2 text-2xl font-bold">Reset your password</h1>
            <p className="text-muted-foreground">
              Enter your email to receive a password reset link
            </p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="bg-card border-border/40 rounded-lg border p-6"
          >
            {!isSubmitted ? (
              <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                  <FormField
                    control={form.control}
                    name="email"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Email</FormLabel>
                        <FormControl>
                          <Input
                            placeholder="name@example.com"
                            type="email"
                            className="h-11"
                            disabled={forgotPasswordMutation.isPending}
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <Button
                    type="submit"
                    className="h-11 w-full"
                    disabled={forgotPasswordMutation.isPending}
                  >
                    {forgotPasswordMutation.isPending ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Sending link...
                      </>
                    ) : (
                      'Send reset link'
                    )}
                  </Button>
                </form>
              </Form>
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
                  <span className="text-foreground font-medium">{submittedEmail}</span>
                </p>
                <Button
                  variant="outline"
                  className="h-11 w-full"
                  onClick={() => setIsSubmitted(false)}
                >
                  Back to reset password
                </Button>
              </motion.div>
            )}
          </motion.div>

          <div className="mt-8 space-y-2 text-center">
            <Link
              href="/login"
              className="text-muted-foreground hover:text-primary text-sm transition-colors"
            >
              Back to login
            </Link>
            <div>
              <Link
                href="/"
                className="text-muted-foreground hover:text-primary mt-4 flex items-center justify-center gap-1 text-xs transition-colors"
              >
                <ArrowLeft className="h-3 w-3" />
                Back to Home
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
