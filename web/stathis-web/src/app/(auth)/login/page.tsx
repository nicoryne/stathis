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
import { loginWithEmail, loginWithOAuth } from '@/services/api-auth-client';
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
import { Provider } from '@/services/api-auth-client';
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
    onError: (error: any) => {
      // Check if the error message mentions invalid credentials or 401/403
      const errorMessage = error.message || '';
      
      if (errorMessage.includes('Invalid email or password') || 
          errorMessage.includes('check your credentials')) {
        // User-friendly message for authentication errors
        toast.error('Invalid credentials', {
          description: 'The email or password you entered is incorrect. Please try again.'
        });
      } else if (error.status === 401 || error.status === 403) {
        // Handle unauthorized/forbidden responses
        toast.error('Authentication failed', {
          description: 'Your account could not be verified. Please check your credentials.'
        });
      } else {
        // Generic error handling for other cases
        toast.error('Login failed', {
          description: errorMessage || 'An unexpected error occurred. Please try again later.'
        });
      }
    }
  });

  const loginOAuthMutation = useMutation({
    mutationFn: loginWithOAuth,
    onSuccess: () => {},
    onError: (error: any) => {
      // Check if the error message mentions invalid credentials or 401/403
      const errorMessage = error.message || '';
      
      if (errorMessage.includes('Invalid') || 
          errorMessage.includes('credentials')) {
        // User-friendly message for authentication errors
        toast.error('Authentication failed', {
          description: 'Your account could not be verified with this provider. Please try again.'
        });
      } else if (error.status === 401 || error.status === 403) {
        // Handle unauthorized/forbidden responses
        toast.error('Provider login failed', {
          description: 'We couldn\'t authenticate you with this provider. Please try another method.'
        });
      } else {
        // Generic error handling for other cases
        toast.error('Login failed', {
          description: errorMessage || 'An unexpected error occurred. Please try another login method.'
        });
      }
    }
  });

  const onSubmitEmail = (data: LoginFormValues) => {
    loginEmailMutation.mutate(data);
  };

  const onSubmitOAuth = (provider: Provider) => {
    loginOAuthMutation.mutate(provider);
  };

  return (
    <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-background via-background to-muted/20">
      {/* Animated Particles */}
      <div className="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
        <motion.div className="absolute left-6 top-6 h-32 w-32 rounded-full bg-primary/5" animate={{ scale: [1, 1.05, 1] }} transition={{ duration: 6, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute right-8 top-10 h-24 w-24 rounded-full bg-secondary/5" animate={{ y: [0, -10, 0] }} transition={{ duration: 5, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute bottom-8 left-8 h-40 w-40 rounded-full bg-primary/5" animate={{ scale: [1, 1.08, 1] }} transition={{ duration: 7, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute bottom-10 right-12 h-28 w-28 rounded-full bg-secondary/5" animate={{ y: [0, -12, 0] }} transition={{ duration: 6, repeat: Number.POSITIVE_INFINITY }} />
      </div>

      <div className="mx-auto grid min-h-screen max-w-6xl grid-cols-1 gap-8 p-4 lg:grid-cols-2">
        {/* Left Column - Mascot & Welcome */}
        <div className="flex flex-col items-center justify-center gap-8 p-8 text-center lg:items-center lg:text-center">
          {/* Logo section removed as requested */}

          {/* Mascot with glow */}
          <div className="relative self-center">
            <div className="absolute -inset-8 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-3xl" />
            <motion.img
              src="/images/mascots/mascot_teacher.png"
              alt="Stathis Mascot"
              className="relative mx-auto h-[200px] w-[200px] drop-shadow-2xl"
              animate={{ y: [0, -10, 0] }}
              transition={{ duration: 2, repeat: Number.POSITIVE_INFINITY }}
            />
          </div>

          {/* Welcome text */}
          <div className="space-y-4">
            <h1 className="text-balance text-4xl font-bold sm:text-5xl">Welcome Back!</h1>
            <p className="mx-auto max-w-md text-pretty text-lg text-muted-foreground">
              Continue your statistics learning journey with AI-powered insights and personalized progress tracking.
            </p>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-6 justify-items-center">
            <div className="text-center">
              <div className="text-2xl font-bold text-primary">10K+</div>
              <div className="text-sm text-muted-foreground">Students</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-secondary">95%</div>
              <div className="text-sm text-muted-foreground">Success Rate</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-primary">50+</div>
              <div className="text-sm text-muted-foreground">Courses</div>
            </div>
          </div>
        </div>

        {/* Right Column - Form Card */}
        <div className="flex w-full items-center justify-center">
          <div className="w-full max-w-md rounded-3xl border border-border/50 bg-card/80 p-8 shadow-2xl backdrop-blur-xl">
            {/* Back link */}
            <Link href="/" className="mb-6 inline-flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-primary">
              <ArrowLeft className="h-4 w-4" /> Back to Home
            </Link>

            {/* Form header */}
            <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }} className="mb-8 text-center">
              <h2 className="mb-2 text-3xl font-bold">Sign In</h2>
              <p className="text-muted-foreground">Enter your credentials to access your dashboard</p>
            </motion.div>

            {/* Form */}
            <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4, delay: 0.05 }}>
              <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmitEmail)} className="space-y-4">
                  {/* Email */}
                  <FormField
                    control={form.control}
                    name="email"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel className="text-sm font-medium">Email</FormLabel>
                        <FormControl>
                          <Input
                            placeholder="name@example.com"
                            type="email"
                            className="h-12 bg-background/50 border-border/50"
                            disabled={loginEmailMutation.isPending}
                            required
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  {/* Password */}
                  <FormField
                    control={form.control}
                    name="password"
                    render={({ field }) => (
                      <FormItem>
                        <div className="mb-1.5 flex items-center justify-between">
                          <FormLabel className="text-sm font-medium">Password</FormLabel>
                          <Link href="/forgot-password" className="text-muted-foreground hover:text-primary text-xs transition-colors">Forgot password?</Link>
                        </div>
                        <FormControl>
                          <PasswordInput
                            placeholder="••••••••"
                            className="h-12 pr-10"
                            disabled={loginEmailMutation.isPending}
                            required
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  {/* Remember me */}
                  <FormField
                    control={form.control}
                    name="rememberMe"
                    render={({ field }) => (
                      <FormItem className="flex flex-row items-center space-y-0 space-x-2">
                        <FormControl>
                          <Checkbox checked={field.value} onCheckedChange={field.onChange} disabled={loginEmailMutation.isPending} />
                        </FormControl>
                        <FormLabel className="cursor-pointer text-sm font-medium">Remember me for 30 days</FormLabel>
                      </FormItem>
                    )}
                  />

                  {/* Submit */}
                  <Button type="submit" className="h-12 w-full rounded-xl bg-gradient-to-r from-primary to-primary/90 shadow-lg transition-shadow hover:shadow-xl font-medium" disabled={loginEmailMutation.isPending}>
                    {loginEmailMutation.isPending ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Signing in...
                      </>
                    ) : (
                      'Sign In'
                    )}
                  </Button>
                </form>
              </Form>

              {/* Divider */}
              <div className="mt-8 mb-6">
                <div className="relative">
                  <div className="absolute inset-0 flex items-center">
                    <Separator />
                  </div>
                  <div className="relative flex justify-center">
                    <span className="bg-background text-muted-foreground px-2 text-xs">Or continue with</span>
                  </div>
                </div>
              </div>

              {/* Social buttons */}
              <div className="grid grid-cols-2 gap-3">
                <Button variant="outline" className="h-12 rounded-xl bg-background/50 font-normal" disabled={loginOAuthMutation.isPending} onClick={() => onSubmitOAuth('google')}>
                  <Mail className="mr-2 h-4 w-4" /> Google
                </Button>
                <Button variant="outline" className="h-12 rounded-xl bg-background/50 font-normal" disabled={loginOAuthMutation.isPending} onClick={() => onSubmitOAuth('azure')}>
                  <Microsoft className="mr-2 h-4 w-4" /> Microsoft
                </Button>
              </div>

              {/* Footer link */}
              <div className="mt-8 space-y-2 text-center">
                <Link href="/sign-up" className="text-muted-foreground hover:text-primary text-sm transition-colors">
                  Don't have an account? <span className="text-primary font-medium">Sign up</span>
                </Link>
              </div>
            </motion.div>
          </div>
        </div>
      </div>
    </div>
  );
}