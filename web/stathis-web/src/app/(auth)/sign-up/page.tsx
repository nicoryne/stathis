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
      toast.success('Sign Up successful', {
        description: 'Please verify your email to continue'
      });
      setSignedUpEmail(form.getValues().email);
      setShowVerificationModal(true);
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
      
      // For other errors, show the standard error message
      const errorMessage = error.message && error.message !== '""' 
        ? error.message 
        : 'Existing email, please try logging in or verifying.';
        
      toast.error('Error Signing Up', {
        description: errorMessage
      });
    }
  });

  const onSubmit = (data: SignUpFormValues) => {
    signUpMutation.mutate(data);
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
              Join Stathis Today
            </motion.h1>
            <motion.p
              className="mb-12 max-w-md text-lg text-white/90"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 }}
            >
              Create your account and start monitoring physical education with AI-powered safety
              tools
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
                  <h3 className="mb-1 font-medium">Comprehensive Dashboard</h3>
                  <p className="text-sm text-white/80">
                    Monitor all your students from a single interface
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
                  <h3 className="mb-1 font-medium">Data Security</h3>
                  <p className="text-sm text-white/80">
                    Your students' health data is encrypted and protected
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
                  <h3 className="mb-1 font-medium">Team Collaboration</h3>
                <p className="text-sm text-white/80">
                    Invite colleagues to collaborate on student monitoring
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

      {/* Right Panel - Sign Up Form */}
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
            <h1 className="mb-2 text-2xl font-bold">Create an account</h1>
            <p className="text-muted-foreground">Sign up for Stathis to get started</p>
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

                <Button type="submit" className="h-11 w-full" disabled={signUpMutation.isPending}>
                  {signUpMutation.isPending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Signing Up...
                    </>
                  ) : (
                    'Sign Up'
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
                className="h-11 font-normal"
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

      {/* Verification Modal */}
      <VerificationModal
        isOpen={showVerificationModal}
        onOpenChange={setShowVerificationModal}
        email={signedUpEmail}
      />
    </div>
  );
}
