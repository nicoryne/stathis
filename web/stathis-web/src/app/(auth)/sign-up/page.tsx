'use client';
import { useEffect, useState } from 'react';
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
import { isUserVerified, signUp } from '@/services/api-auth-client';
import { signUpSchema, type SignUpFormValues } from '@/lib/validations/auth';
import { useFormValidation } from '@/hooks/use-form-validation';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form';
import { Checkbox } from '@/components/ui/checkbox';
import { VerificationModal } from '@/components/auth/verification-modal';
import { PasswordStrengthIndicator } from '@/components/auth/password-strength-indicator';
import { PasswordInput } from '@/components/auth/password-input';
import { toast } from 'sonner';

export default function SignUpPage() {
  const [showVerificationModal, setShowVerificationModal] = useState(false);
  const [signedUpEmail, setSignedUpEmail] = useState('');
  const [password, setPassword] = useState('');

  const form = useFormValidation(signUpSchema, {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    terms: false
  });

  const signUpMutation = useMutation({
    mutationFn: signUp,
    onSuccess: (data) => {
      console.log('[Sign Up] Success response:', data);
      
      // Ensure we have the user's email, either from the response or from the form
      const userEmail = 
        (data && typeof data === 'object' && 'email' in data) ? 
        (data.email as string) : form.getValues().email;
      
      toast.success('Sign up successful', {
        description: 'Please proceed to login'
      });
      
      // Always set the email and show verification modal
      setSignedUpEmail(userEmail);
      setShowVerificationModal(true);
      
      // Extra logging to confirm verification modal should be shown
      console.log('[Sign Up] Showing verification modal for:', userEmail);
      
    },
    onError: (error: any) => {
      console.error('[Sign Up] Error details:', error);
      
      // Check if this is our custom EmailAlreadyInUseError or has an email-related error message
      if ((error.name === 'EmailAlreadyInUseError') || 
          (error.message && (error.message.includes('Email is already in use') || 
                             error.message.includes('already exists') || 
                             error.message.includes('already registered')))) {
        
        // Show a helpful message asking the user to verify their email
        toast.info('Account already exists', {
          description: 'Please verify your account from the email you provided or try logging in.',
          duration: 5000
        });
        
        // Show the verification modal with the email they tried to register
        setSignedUpEmail(form.getValues().email);
        setShowVerificationModal(true);
        return;
      }
      
      // Special case for 401 errors which might be due to duplicate email
      if (error.status === 401 || 
          (typeof error === 'object' && error.message === '')) {
        console.log('[Sign Up] Handling possible duplicate email 401 error');
        toast.info('Account may already exist', {
          description: 'If you already have an account, please verify your email or try logging in.',
          duration: 5000
        });
        return;
      }
      
      // For 403/401 errors which are likely non-existent accounts or password issues
      if (error.status === 403 || error.status === 401) {
        toast.error('Account error', {
          description: 'We couldn\'t create your account. This email might be in use or invalid.'
        });
        return;
      }
      
      // For other errors, show the standard error message
      const errorMessage = error.message && error.message !== '""' 
        ? error.message 
        : 'Something went wrong while creating your account. Please try again.';
        
      toast.error('Error signing up', {
        description: errorMessage
      });
    }
  });

  const onSubmit = (data: SignUpFormValues) => {
    signUpMutation.mutate(data);
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
          {/* Logo removed as requested */}

          {/* Mascot */}
          <div className="relative self-center">
            <div className="absolute -inset-8 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-3xl" />
            <motion.img
              src="/images/mascots/mascot_celebrate.png"
              alt="STATHIS Mascot"
              className="relative mx-auto h-[220px] w-[220px] drop-shadow-2xl"
              animate={{ y: [0, -10, 0] }}
              transition={{ duration: 2, repeat: Number.POSITIVE_INFINITY }}
            />
          </div>

          {/* Welcome Text */}
          <div className="space-y-3 max-w-2xl mx-auto text-center">
            <h1 className="text-balance text-4xl font-bold sm:text-5xl">Create your STATHIS account</h1>
            <p className="mx-auto max-w-md text-pretty text-lg text-muted-foreground">Join thousands learning statistics with interactive lessons, real-world examples, and personalized paths.</p>
          </div>

          {/* Feature list */}
          <div className="grid w-full max-w-md grid-cols-1 gap-4 mx-auto justify-items-center">
            <div className="flex items-center gap-3 justify-center">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10">
                <Shield className="h-4 w-4 text-primary" />
              </div>
              <span className="text-sm text-foreground text-center">Secure, privacy-first platform</span>
            </div>
            <div className="flex items-center gap-3 justify-center">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-secondary/10">
                <Users className="h-4 w-4 text-secondary" />
              </div>
              <span className="text-sm text-foreground text-center">Collaborative classroom tools</span>
            </div>
            <div className="flex items-center gap-3 justify-center">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10">
                <Activity className="h-4 w-4 text-primary" />
              </div>
              <span className="text-sm text-foreground text-center">Interactive, engaging learning</span>
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
              <h2 className="mb-2 text-3xl font-bold">Create Account</h2>
              <p className="text-muted-foreground">Sign up to begin your learning journey</p>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 }}
            >
            <Form {...form}>
              <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <FormField
                    control={form.control}
                    name="firstName"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>First Name</FormLabel>
                        <FormControl>
                          <Input
                            placeholder="John"
                            className="h-11"
                            disabled={signUpMutation.isPending}
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="lastName"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Last Name</FormLabel>
                        <FormControl>
                          <Input
                            placeholder="Doe"
                            className="h-11"
                            disabled={signUpMutation.isPending}
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

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
                          disabled={signUpMutation.isPending}
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
                      <FormLabel>Password</FormLabel>
                      <FormControl>
                        <PasswordInput
                          placeholder="••••••••"
                          className="h-11"
                          disabled={signUpMutation.isPending}
                          onChange={(e) => {
                            field.onChange(e);
                            setPassword(e.target.value);
                          }}
                          value={field.value}
                        />
                      </FormControl>
                      <PasswordStrengthIndicator password={password} />
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="confirmPassword"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Confirm Password</FormLabel>
                      <FormControl>
                        <PasswordInput
                          placeholder="••••••••"
                          className="h-11"
                          disabled={signUpMutation.isPending}
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="terms"
                  render={({ field }) => (
                    <FormItem className="flex flex-row items-start space-y-0 space-x-2">
                      <FormControl>
                        <Checkbox
                          checked={field.value}
                          onCheckedChange={field.onChange}
                          disabled={signUpMutation.isPending}
                        />
                      </FormControl>
                      <div className="leading-tight">
                        <FormLabel className="text-xs font-normal">
                          I agree to the{' '}
                          <a href="#" className="hover:text-primary underline underline-offset-2">
                            Terms of Service
                          </a>{' '}
                          and{' '}
                          <a href="#" className="hover:text-primary underline underline-offset-2">
                            Privacy Policy
                          </a>
                        </FormLabel>
                      </div>
                    </FormItem>
                  )}
                />

                <Button type="submit" className="h-12 w-full rounded-xl bg-gradient-to-r from-primary to-primary/90 shadow-lg hover:shadow-xl transition-shadow" disabled={signUpMutation.isPending}>
                  {signUpMutation.isPending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Signing Up...
                    </>
                  ) : (
                    'Create Account'
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
                className="h-12 rounded-xl font-normal bg-background/50"
                disabled={signUpMutation.isPending}
                onClick={() => {
                  toast.info('Microsoft login is not implemented in this demo');
                }}
              >
                <Microsoft className="mr-2 h-4 w-4" />
                Microsoft
              </Button>
              <Button
                variant="outline"
                className="h-12 rounded-xl font-normal bg-background/50"
                disabled={signUpMutation.isPending}
                onClick={() => {
                  toast.info('Google login is not implemented in this demo');
                }}
              >
                <Mail className="mr-2 h-4 w-4" />
                Google
              </Button>
            </div>

            <div className="mt-8 space-y-2 text-center">
              <Link
                href="/login"
                className="text-muted-foreground hover:text-primary text-sm transition-colors"
              >
                Already have an account? <span className="text-primary font-medium">Sign in</span>
              </Link>
            </div>
          </motion.div>
        </div>
      </div>

      {/* Verification Modal */}
      <VerificationModal
        isOpen={showVerificationModal}
        onOpenChange={setShowVerificationModal}
        email={signedUpEmail}
      />
    </div>
    </div>
  );
}
