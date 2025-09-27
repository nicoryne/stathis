'use client';
import Image from 'next/image';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export function Logo() {
  const pathname = usePathname();

  const handleLogoClick = (e: React.MouseEvent) => {
    if (pathname === '/') {
      e.preventDefault();
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    }
  };

  return (
    <Link 
      href="/" 
      className="flex items-center gap-3 hover:opacity-80 transition-opacity"
      onClick={handleLogoClick}
    >
      <Image 
        src="/images/logos/stathis.webp" 
        alt="Stathis Logo" 
        width={40} 
        height={40}
        className="rounded-lg"
      />
      <span className="text-xl font-bold tracking-tight text-primary">Stathis</span>
    </Link>
  );
}
