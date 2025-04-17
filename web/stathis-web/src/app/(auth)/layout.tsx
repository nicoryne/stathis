import { BackgroundTexture } from '@/components/background-texture';

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <BackgroundTexture />

      {children}
    </>
  );
}
