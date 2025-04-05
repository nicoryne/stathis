import { HeartPulse } from 'lucide-react';

export function Logo() {
  return (
    <div className="flex items-center gap-2">
      <HeartPulse className="text-primary h-8 w-8" />
      <span className="text-xl font-bold tracking-tight">Stathis</span>
    </div>
  );
}
