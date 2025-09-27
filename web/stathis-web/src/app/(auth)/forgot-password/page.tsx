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
import { forgotPassword } from '@/services/api-auth-client';
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
    <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-background to-muted/20">
      {/* Animated Particles */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <motion.div className="absolute left-6 top-6 h-32 w-32 rounded-full bg-primary/5" animate={{ scale: [1, 1.05, 1] }} transition={{ duration: 6, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute right-8 top-10 h-24 w-24 rounded-full bg-secondary/5" animate={{ y: [0, -10, 0] }} transition={{ duration: 5, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute bottom-8 left-8 h-40 w-40 rounded-full bg-primary/5" animate={{ scale: [1, 1.08, 1] }} transition={{ duration: 7, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute bottom-10 right-12 h-28 w-28 rounded-full bg-secondary/5" animate={{ y: [0, -12, 0] }} transition={{ duration: 6, repeat: Number.POSITIVE_INFINITY }} />
      </div>

      <div className="relative mx-auto grid min-h-screen max-w-7xl grid-cols-1 gap-12 p-6 lg:grid-cols-2 lg:p-12">
        {/* Left Column - Mascot & Welcome */}
        <div className="flex flex-col items-center justify-center gap-8 text-center lg:items-center lg:text-center">
          {/* Mascot */}
          <div className="relative self-center">
            <div className="absolute -inset-8 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-3xl" />
            <motion.img
              src="/images/mascots/mascot_error.png"
              alt="STATHIS Mascot"
              className="relative mx-auto h-[220px] w-[220px] drop-shadow-2xl"
              animate={{ y: [0, -10, 0] }}
              transition={{ duration: 2, repeat: Number.POSITIVE_INFINITY }}
            />
          </div>

          {/* Welcome Text */}
          <div className="space-y-3 max-w-2xl mx-auto text-center">
            <h1 className="text-balance text-4xl font-bold sm:text-5xl">Reset your password</h1>
            <p className="mx-auto max-w-md text-pretty text-lg text-muted-foreground">Don't worry, we'll help you get back to monitoring your students' safety in no time.</p>
          </div>

          {/* Feature list */}
          <div className="grid w-full max-w-md grid-cols-1 gap-4 mx-auto justify-items-center">
            <div className="flex items-center gap-3 justify-center">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10">
                <Activity className="h-4 w-4 text-primary" />
              </div>
              <span className="text-sm text-foreground text-center">Simple recovery process</span>
            </div>
            <div className="flex items-center gap-3 justify-center">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-secondary/10">
                <Shield className="h-4 w-4 text-secondary" />
              </div>
              <span className="text-sm text-foreground text-center">Secure password reset</span>
            </div>
            <div className="flex items-center gap-3 justify-center">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10">
                <Users className="h-4 w-4 text-primary" />
              </div>
              <span className="text-sm text-foreground text-center">Support available if needed</span>
            </div>
          </div>
        </div>

        {/* Right Column - Form Card */}
        <div className="flex w-full items-center justify-center p-2">
          <div className="w-full max-w-lg rounded-3xl border border-border/50 bg-card/80 p-8 shadow-2xl backdrop-blur-xl">
            {/* Back link */}
            <Link href="/" className="mb-6 inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-primary">
              <ArrowLeft className="h-4 w-4" /> Back to Home
            </Link>
            
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
              className="mb-8 text-center"
            >
              <h2 className="mb-2 text-3xl font-bold">Reset Password</h2>
              <p className="text-muted-foreground">Enter your email to receive a reset link</p>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 }}
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
                      className="h-12 w-full rounded-xl bg-gradient-to-r from-primary to-primary/90 shadow-lg hover:shadow-xl transition-shadow"
                      disabled={forgotPasswordMutation.isPending}
                    >
                      {forgotPasswordMutation.isPending ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Sending Reset Link...
                        </>
                      ) : (
                        'Send Reset Link'
                      )}
                    </Button>
                  </form>
                </Form>
              ) : (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="py-8 text-center"
                >
                  <div className="mb-6 flex justify-center">
                    <div className="relative">
                      <div className="absolute -inset-4 rounded-full bg-gradient-to-br from-green-500/20 to-emerald-500/20 blur-xl" />
                      <CheckCircle2 className="relative text-green-600 h-16 w-16" />
                    </div>
                  </div>
                  <h3 className="mb-3 text-xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">Check your email</h3>
                  <p className="text-muted-foreground mb-6">
                    We've sent a password reset link to{' '}
                    <span className="text-foreground font-semibold">{submittedEmail}</span>
                  </p>
                  <Button
                    variant="outline"
                    className="h-12 rounded-xl font-normal bg-background/50 backdrop-blur-sm border-border/50 hover:bg-muted/50 transition-all duration-200"
                    onClick={() => setIsSubmitted(false)}
                  >
                    Back to Reset Password
                  </Button>
                </motion.div>
              )}
            </motion.div>

            <div className="mt-8 space-y-2 text-center">
              <Link
                href="/login"
                className="text-muted-foreground hover:text-primary text-sm transition-colors"
              >
                Remember your password? <span className="text-primary font-medium">Sign in</span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}