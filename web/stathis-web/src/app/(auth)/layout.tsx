import { BackgroundTexture } from '@/components/background-texture';
import { Logo } from '@/components/logo';
import Link from 'next/link';

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <BackgroundTexture />

      {children}
    </>
  );
}
