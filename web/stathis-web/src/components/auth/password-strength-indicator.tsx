'use client';

import { Check, X } from 'lucide-react';
import { useState, useEffect } from 'react';

interface PasswordRequirement {
  label: string;
  regex: RegExp;
  met: boolean;
}

interface PasswordStrengthIndicatorProps {
  password: string;
}

export function PasswordStrengthIndicator({ password }: PasswordStrengthIndicatorProps) {
  const [requirements, setRequirements] = useState<PasswordRequirement[]>([
    { label: 'At least 8 characters', regex: /.{8,}/, met: false },
    { label: 'At least one lowercase letter', regex: /[a-z]/, met: false },
    { label: 'At least one uppercase letter', regex: /[A-Z]/, met: false },
    { label: 'At least one number', regex: /[0-9]/, met: false },
    { label: 'At least one special character', regex: /[^A-Za-z0-9]/, met: false }
  ]);

  useEffect(() => {
    setRequirements((prevRequirements) =>
      prevRequirements.map((requirement) => ({
        ...requirement,
        met: requirement.regex.test(password)
      }))
    );
  }, [password]);

  const strengthPercentage = Math.min(
    100,
    Math.round((requirements.filter((req) => req.met).length / requirements.length) * 100)
  );

  const getStrengthColor = () => {
    if (strengthPercentage < 40) return 'bg-red-500';
    if (strengthPercentage < 80) return 'bg-yellow-500';
    return 'bg-green-500';
  };

  return (
    <div className="mt-2 space-y-3">
      <div className="space-y-2">
        <div className="bg-muted h-1.5 w-full overflow-hidden rounded-full">
          <div
            className={`h-full transition-all duration-300 ${getStrengthColor()}`}
            style={{ width: `${strengthPercentage}%` }}
          />
        </div>
        <div className="text-muted-foreground text-xs">
          Password strength:{' '}
          {strengthPercentage === 100 ? 'Strong' : strengthPercentage >= 60 ? 'Medium' : 'Weak'}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-2 text-xs sm:grid-cols-2">
        {requirements.map((requirement, index) => (
          <div key={index} className="flex items-center gap-2">
            <div
              className={`flex h-4 w-4 items-center justify-center rounded-full ${
                requirement.met
                  ? 'bg-green-500/20 text-green-600'
                  : 'bg-muted-foreground/20 text-muted-foreground'
              }`}
            >
              {requirement.met ? <Check className="h-3 w-3" /> : <X className="h-3 w-3" />}
            </div>
            <span className={requirement.met ? 'text-foreground' : 'text-muted-foreground'}>
              {requirement.label}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
