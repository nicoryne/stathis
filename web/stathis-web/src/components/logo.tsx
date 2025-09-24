import Image from 'next/image';
import Link from 'next/link';

export function Logo() {
  return (
    <Link href="/" className="flex items-center gap-3 hover:opacity-80 transition-opacity">
      <Image 
        src="/images/logos/stathis.webp" 
        alt="STATHIS Logo" 
        width={40} 
        height={40}
        className="rounded-lg"
      />
      <span className="text-xl font-bold tracking-tight text-primary">STATHIS</span>
    </Link>
  );
}
