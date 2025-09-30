'use client';
import { useRouter } from 'next/navigation';
import { DialogFooter } from '@/components/ui/dialog';

import { useMutation } from '@tanstack/react-query';
import { Loader2, Mail, RefreshCw, HeartPulse, CheckCircle } from 'lucide-react';
import { motion } from 'framer-motion';
import { toast } from 'sonner';

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { resendEmailVerification } from '@/services/api-auth-client';

interface VerificationModalProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  email: string;
}

export function VerificationModal({ isOpen, onOpenChange, email }: VerificationModalProps) {
  const router = useRouter();

  const resendMutation = useMutation({
    mutationFn: () => resendEmailVerification(email),
    onSuccess: () => {
      toast.success('Verification email sent', {
        description: 'Please check your inbox for the verification link'
      });
    },
    onError: () => {
      toast.error('Failed to send verification email', {
        description: 'Please try again later'
      });
    }
  });

  const handleResend = () => {
    resendMutation.mutate();
  };

  const handleGoToLogin = () => {
    onOpenChange(false);
    router.push('/login');
  };

  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg rounded-3xl border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl">
        {/* Animated background elements */}
        <div className="absolute inset-0 overflow-hidden rounded-3xl">
          <motion.div
            className="absolute -top-4 -right-4 h-24 w-24 rounded-full bg-primary/5"
            animate={{
              scale: [1, 1.1, 1],
              rotate: [0, 180, 360]
            }}
            transition={{
              duration: 8,
              repeat: Number.POSITIVE_INFINITY,
              ease: "linear"
            }}
          />
          <motion.div
            className="absolute -bottom-4 -left-4 h-32 w-32 rounded-full bg-secondary/5"
            animate={{
              scale: [1, 1.05, 1],
              rotate: [360, 180, 0]
            }}
            transition={{
              duration: 10,
              repeat: Number.POSITIVE_INFINITY,
              ease: "linear"
            }}
          />
        </div>

         <div className="relative z-10">
           <DialogHeader className="text-center space-y-6">
             {/* Success Icon */}
             <motion.div
               initial={{ opacity: 0, scale: 0 }}
               animate={{ opacity: 1, scale: 1 }}
               transition={{ duration: 0.5 }}
               className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-green-500/20 to-emerald-500/20"
             >
               <CheckCircle className="h-8 w-8 text-green-600" />
             </motion.div>

             <motion.div
               initial={{ opacity: 0, y: 20 }}
               animate={{ opacity: 1, y: 0 }}
               transition={{ duration: 0.5, delay: 0.2 }}
             >
               <DialogTitle className="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent text-center">
                 Account Created Successfully!
               </DialogTitle>
               <DialogDescription className="text-base text-muted-foreground mt-2">
                 We've sent a verification email to{' '}
                 <span className="text-foreground font-semibold">{email}</span>
               </DialogDescription>
             </motion.div>
           </DialogHeader>

           <motion.div
             initial={{ opacity: 0, y: 20 }}
             animate={{ opacity: 1, y: 0 }}
             transition={{ duration: 0.5, delay: 0.3 }}
             className="py-6"
           >
            <div className="rounded-2xl border border-border/50 bg-gradient-to-br from-muted/30 to-muted/10 p-6 backdrop-blur-sm">
              <div className="flex items-start gap-3 mb-4">
                <Mail className="h-5 w-5 text-primary mt-0.5 flex-shrink-0" />
                <div>
                  <h3 className="font-semibold text-foreground mb-2">Next Steps:</h3>
                  <ul className="space-y-2 text-sm text-muted-foreground">
                    <li className="flex items-start gap-2">
                      <span className="text-primary font-bold">1.</span>
                      <span>Check your email inbox for the verification link</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-primary font-bold">2.</span>
                      <span>Click the link to verify your account</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <span className="text-primary font-bold">3.</span>
                      <span>If you don't see the email, check your spam folder</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </motion.div>

          <DialogFooter className="flex flex-col gap-3 sm:flex-row pt-4">
            <Button
              variant="outline"
              className="h-12 rounded-xl font-normal bg-background/50 backdrop-blur-sm border-border/50 hover:bg-muted/50 transition-all duration-200"
              onClick={handleResend}
              disabled={resendMutation.isPending}
            >
              {resendMutation.isPending ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Sending...
                </>
              ) : (
                <>
                  <RefreshCw className="mr-2 h-4 w-4" />
                  Resend Email
                </>
              )}
            </Button>
            <Button 
              className="h-12 rounded-xl bg-gradient-to-r from-primary to-primary/90 shadow-lg hover:shadow-xl transition-all duration-200 font-medium" 
              onClick={handleGoToLogin}
            >
              Go to Login
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}
