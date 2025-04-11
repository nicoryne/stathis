'use client';
import { useRouter } from 'next/navigation';
import { DialogFooter } from '@/components/ui/dialog';

import { useMutation } from '@tanstack/react-query';
import { Loader2, Mail, RefreshCw } from 'lucide-react';
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
import { resendEmailVerification } from '@/services/auth';

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
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="bg-primary/10 mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full">
            <Mail className="text-primary h-6 w-6" />
          </div>
          <DialogTitle className="text-center text-xl">Verify your email</DialogTitle>
          <DialogDescription className="text-center">
            We've sent a verification email to{' '}
            <span className="text-foreground font-medium">{email}</span>
          </DialogDescription>
        </DialogHeader>

        <div className="py-4">
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="bg-muted/50 rounded-lg border p-4 text-sm"
          >
            <p className="mb-2 font-medium">Before you can log in:</p>
            <ul className="ml-4 list-disc space-y-2">
              <li>Check your email inbox for a verification link</li>
              <li>Click the link to verify your account</li>
              <li>If you don't see the email, check your spam folder</li>
            </ul>
          </motion.div>
        </div>

        <DialogFooter className="flex flex-col gap-2 sm:flex-row">
          <Button
            variant="outline"
            className="w-full sm:w-auto"
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
                Resend email
              </>
            )}
          </Button>
          <Button className="w-full sm:w-auto" onClick={handleGoToLogin}>
            Go to login
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
