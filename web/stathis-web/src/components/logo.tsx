import { HeartPulse } from 'lucide-react';
import Image from 'next/image';

export function Logo() {
  return (
    <div className="flex items-center gap-2">
      <Image src="/images/logos/stathis.webp" width={32} height={32} alt="Stathis Logo" />
      <span className="text-xl font-bold tracking-tight">Stathis</span>
    </div>
  );
}
