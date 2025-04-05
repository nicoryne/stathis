import { BackgroundTexture } from '@/components/background-texture';
import { Logo } from '@/components/logo';
import Link from 'next/link';

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid min-h-screen place-items-center p-4">
      <BackgroundTexture />

      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <Link href="/" className="mb-6 inline-block">
            <Logo />
          </Link>
          {children}
        </div>
      </div>
    </div>
  );
}
