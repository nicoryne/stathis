'use client';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Separator } from '@/components/ui/separator';
import {
  Loader2,
  Mail,
  ComputerIcon as Microsoft,
  HeartPulse,
  Activity,
  Shield,
  Users,
  ArrowLeft
} from 'lucide-react';
import Link from 'next/link';
import { motion } from 'framer-motion';
import { useRouter } from 'next/navigation';
import { useMutation } from '@tanstack/react-query';
import { loginWithEmail, loginWithOAuth } from '@/services/auth';
import { loginSchema, type LoginFormValues } from '@/lib/validations/auth';
import { useFormValidation } from '@/hooks/use-form-validation';
import { toast } from 'sonner';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { Checkbox } from '@/components/ui/checkbox';
import { Provider } from '@supabase/supabase-js';
import { PasswordInput } from '@/components/auth/password-input';

export default function LoginPage() {
  const router = useRouter();

  const form = useFormValidation(loginSchema, {
    email: '',
    password: '',
    rememberMe: false
  });

  const loginEmailMutation = useMutation({
    mutationFn: loginWithEmail,
    onSuccess: () => {
      toast.success('Login successful', {
        description: 'Redirecting to dashboard...'
      });
      router.replace('/dashboard');
    },
    onError: (error) => {
      toast.error('Login failed', {
        description: error.message || 'Please check your credentials and try again'
      });
    }
  });

  const loginOAuthMutation = useMutation({
    mutationFn: loginWithOAuth,
    onSuccess: () => {},
    onError: (error) => {
      toast.error('Login failed', {
        description: error.message || 'Please check your credentials and try again'
      });
    }
  });

  const onSubmitEmail = (data: LoginFormValues) => {
    loginEmailMutation.mutate(data);
  };

  const onSubmitOAuth = (provider: Provider) => {
    loginOAuthMutation.mutate(provider);
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
              Welcome to Stathis
            </motion.h1>
            <motion.p
              className="mb-12 max-w-md text-lg text-white/90"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 }}
            >
              AI-Powered Posture and Vitals Monitoring for Safe Physical Education
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
                  <h3 className="mb-1 font-medium">Real-time Monitoring</h3>
                  <p className="text-sm text-white/80">
                    Track posture and vital signs during physical activities
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
                  <h3 className="mb-1 font-medium">Enhanced Safety</h3>
                  <p className="text-sm text-white/80">
                    Prevent injuries with early detection and alerts
                  </p>
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
                  <h3 className="mb-1 font-medium">Personalized Insights</h3>
                  <p className="text-sm text-white/80">
                    Tailored feedback and analytics for each student
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

      {/* Right Panel - Login Form */}
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
            <h1 className="mb-2 text-2xl font-bold">Sign in to your account</h1>
            <p className="text-muted-foreground">Enter your credentials to access your dashboard</p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
          >
            <Form {...form}>
              <form onSubmit={form.handleSubmit(onSubmitEmail)} className="space-y-4">
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
                          disabled={loginEmailMutation.isPending}
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="password"
                  render={({ field }) => (
                    <FormItem>
                      <div className="mb-1.5 flex items-center justify-between">
                        <FormLabel className="mb-0">Password</FormLabel>
                        <Link
                          href="/forgot-password"
                          className="text-muted-foreground hover:text-primary text-xs transition-colors"
                        >
                          Forgot password?
                        </Link>
                      </div>
                      <FormControl>
                        <PasswordInput
                          placeholder="••••••••"
                          className="h-11"
                          disabled={loginEmailMutation.isPending}
                          autoComplete="false"
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="rememberMe"
                  render={({ field }) => (
                    <FormItem className="flex flex-row items-center space-y-0 space-x-2">
                      <FormControl>
                        <Checkbox
                          checked={field.value}
                          onCheckedChange={field.onChange}
                          disabled={loginEmailMutation.isPending}
                        />
                      </FormControl>
                      <FormLabel className="cursor-pointer text-xs font-normal">
                        Remember me for 30 days
                      </FormLabel>
                    </FormItem>
                  )}
                />

                <Button
                  type="submit"
                  className="h-11 w-full"
                  disabled={loginEmailMutation.isPending}
                >
                  {loginEmailMutation.isPending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Signing in...
                    </>
                  ) : (
                    'Sign in'
                  )}
                </Button>
              </form>
            </Form>

            <div className="mt-8 mb-6">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <Separator />
                </div>
                <div className="relative flex justify-center">
                  <span className="bg-background text-muted-foreground px-2 text-xs">
                    Or continue with
                  </span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Button
                variant="outline"
                className="h-11 font-normal"
                disabled={loginOAuthMutation.isPending}
                onClick={() => onSubmitOAuth('azure')}
              >
                <Microsoft className="mr-2 h-4 w-4" />
                Microsoft
              </Button>
              <Button
                variant="outline"
                className="h-11 font-normal"
                disabled={loginOAuthMutation.isPending}
                onClick={() => onSubmitOAuth('google')}
              >
                <Mail className="mr-2 h-4 w-4" />
                Google
              </Button>
            </div>

            <div className="mt-8 space-y-2 text-center">
              <Link
                href="/register"
                className="text-muted-foreground hover:text-primary text-sm transition-colors"
              >
                Don't have an account? <span className="text-primary font-medium">Sign up</span>
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
          </motion.div>
        </div>
      </div>
    </div>
  );
}
