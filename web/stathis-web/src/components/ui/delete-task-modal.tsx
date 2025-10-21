'use client';

import React, { useEffect } from 'react';
import { X } from 'lucide-react';
import { Button } from './button';

interface DeleteTaskModalProps {
  open: boolean;
  onClose: () => void;
  title: string | React.ReactNode;
  description?: string;
  onConfirm: () => void;
  isDeleting?: boolean;
}

export function DeleteTaskModal({
  open,
  onClose,
  title,
  description,
  onConfirm,
  isDeleting = false,
}: DeleteTaskModalProps) {
  // Handle ESC key
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && open && !isDeleting) {
        onClose();
      }
    };
    
    if (open) {
      document.addEventListener('keydown', handleEsc);
      // Prevent body scroll when modal is open
      document.body.style.overflow = 'hidden';
    }
    
    return () => {
      document.removeEventListener('keydown', handleEsc);
      document.body.style.overflow = 'unset';
    };
  }, [open, onClose, isDeleting]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      onClick={isDeleting ? undefined : onClose}
    >
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" />
      
      {/* Modal Content */}
      <div
        className="relative z-10 w-full max-w-md bg-card border border-border rounded-lg shadow-lg animate-in fade-in-0 zoom-in-95"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="p-6">
          <div className="flex items-start gap-4">
            <div className="flex-1">
              <h2 className="text-lg font-semibold flex items-center gap-3">{title}</h2>
              {description && (
                <p className="text-sm text-muted-foreground mt-2">{description}</p>
              )}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex justify-end gap-2 p-6 pt-0">
          <Button
            variant="outline"
            onClick={onClose}
            disabled={isDeleting}
            className="bg-card/80 backdrop-blur-xl border-border/50"
          >
            Cancel
          </Button>
          <Button
            variant="destructive"
            onClick={onConfirm}
            disabled={isDeleting}
            className="bg-destructive hover:bg-destructive/90"
          >
            {isDeleting ? 'Deleting...' : 'Delete'}
          </Button>
        </div>
      </div>
    </div>
  );
}
